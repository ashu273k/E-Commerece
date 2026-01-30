package com.ashu.E_Commerece.service;

import com.ashu.E_Commerece.dto.common.PagedResponse;
import com.ashu.E_Commerece.dto.order.OrderItemResponse;
import com.ashu.E_Commerece.dto.order.OrderRequest;
import com.ashu.E_Commerece.dto.order.OrderResponse;
import com.ashu.E_Commerece.dto.order.OrderStatusUpdateRequest;
import com.ashu.E_Commerece.exception.BadRequestException;
import com.ashu.E_Commerece.exception.ResourceNotFoundException;
import com.ashu.E_Commerece.model.*;
import com.ashu.E_Commerece.repository.OrderRepository;
import com.ashu.E_Commerece.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles order lifecycle: creation, status transitions, and cancellations.
 * All operations are transactional to maintain data consistency between
 * orders, inventory, and cart state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final UserService userService;
    private final EmailService emailService;

    /**
     * Creates an order atomically from the user's cart. This operation:
     * 1. Validates stock availability before reserving inventory
     * 2. Decrements product stock to prevent overselling
     * 3. Clears cart only after successful order creation
     * Rollback occurs if any step fails, restoring original state.
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User user = userService.getCurrentUser();
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // Fail-fast: Check all items before modifying any inventory
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (cartItem.getQuantity() > product.getStockQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
        }

        // Billing defaults to shipping if not provided (common e-commerce pattern)
        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress() != null ? request.getBillingAddress()
                        : request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .status(OrderStatus.PENDING)
                .build();

        // Snapshot product details at order time to preserve historical accuracy
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getEffectivePrice())
                    .build();

            order.addItem(orderItem);

            // Decrement stock immediately to prevent concurrent overselling
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Order calculates its own totals to ensure consistency with business rules
        order.calculateSubtotal();
        order.setShippingCost(calculateShippingCost(order.getSubtotal()));
        order.setTax(calculateTax(order.getSubtotal()));
        order.calculateTotalAmount();

        order = orderRepository.save(order);

        // Cart cleared only after successful order persistence
        cartService.clearCart();

        log.info("Order created: {} for user: {}", order.getOrderNumber(), user.getEmail());

        // Fire-and-forget: email failure should not roll back the order
        emailService.sendOrderConfirmation(user.getEmail(), order);

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getUserOrders(int page, int size) {
        User user = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findByUserId(user.getId(), pageable);
        return mapToPagedResponse(orders);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getAllOrders(int page, int size, OrderStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders;

        if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return mapToPagedResponse(orders);
    }

    /**
     * Returns order if user owns it or is admin. Non-owners receive 404
     * (not 403) to avoid leaking order existence information.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        User user = userService.getCurrentUser();
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Security: return 404 instead of 403 to hide order existence from non-owners
        if (user.getRole() != Role.ADMIN && !order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order", "id", id);
        }

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return mapToResponse(order);
    }

    /**
     * Transitions order status and records timestamps for tracking.
     * Notes are appended (not replaced) to preserve audit trail.
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        if (request.getNotes() != null) {
            String existingNotes = order.getNotes() != null ? order.getNotes() + "\n" : "";
            order.setNotes(existingNotes + request.getNotes());
        }

        // Record milestone timestamps for shipment tracking
        if (request.getStatus() == OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
        } else if (request.getStatus() == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        order = orderRepository.save(order);
        log.info("Order {} status updated from {} to {}", order.getOrderNumber(), oldStatus, request.getStatus());

        emailService.sendOrderStatusUpdate(order.getUser().getEmail(), order);

        return mapToResponse(order);
    }

    /**
     * Cancels order and restores inventory. Only allowed for PENDING/CONFIRMED
     * orders to prevent cancellation of shipped items.
     */
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        User user = userService.getCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Same security pattern as getOrderById
        if (user.getRole() != Role.ADMIN && !order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order", "id", id);
        }

        // Business rule: shipped/delivered orders cannot be cancelled via API
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel order in current status: " + order.getStatus());
        }

        // Restore inventory to make items available for other customers
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        log.info("Order {} cancelled", order.getOrderNumber());

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRecentOrders() {
        return orderRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateShippingCost(BigDecimal subtotal) {
        // Business rule: free shipping threshold
        if (subtotal.compareTo(new BigDecimal("100")) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("9.99");
    }

    private BigDecimal calculateTax(BigDecimal subtotal) {
        // Simplified tax calculation - production would use tax service
        return subtotal.multiply(new BigDecimal("0.10")).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .items(order.getItems().stream()
                        .map(this::mapItemToResponse)
                        .collect(Collectors.toList()))
                .subtotal(order.getSubtotal())
                .shippingCost(order.getShippingCost())
                .tax(order.getTax())
                .discount(order.getDiscount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .paymentId(order.getPaymentId())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    private PagedResponse<OrderResponse> mapToPagedResponse(Page<Order> page) {
        List<OrderResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<OrderResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
