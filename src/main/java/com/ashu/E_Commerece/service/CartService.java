package com.ashu.E_Commerece.service;

import com.ashu.E_Commerece.dto.cart.CartItemRequest;
import com.ashu.E_Commerece.dto.cart.CartItemResponse;
import com.ashu.E_Commerece.dto.cart.CartResponse;
import com.ashu.E_Commerece.exception.BadRequestException;
import com.ashu.E_Commerece.exception.ResourceNotFoundException;
import com.ashu.E_Commerece.model.Cart;
import com.ashu.E_Commerece.model.CartItem;
import com.ashu.E_Commerece.model.Product;
import com.ashu.E_Commerece.model.User;
import com.ashu.E_Commerece.repository.CartItemRepository;
import com.ashu.E_Commerece.repository.CartRepository;
import com.ashu.E_Commerece.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for shopping cart operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    /**
     * Get current user's cart.
     */
    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User user = userService.getCurrentUser();
        Cart cart = getOrCreateCart(user);
        return mapToResponse(cart);
    }

    /**
     * Add item to cart.
     */
    @Transactional
    public CartResponse addItem(CartItemRequest request) {
        User user = userService.getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.isActive()) {
            throw new BadRequestException("Product is not available");
        }

        if (!product.isInStock()) {
            throw new BadRequestException("Product is out of stock");
        }

        if (request.getQuantity() > product.getStockQuantity()) {
            throw new BadRequestException("Requested quantity exceeds available stock");
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new BadRequestException("Total quantity exceeds available stock");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
        }

        cart = cartRepository.save(cart);
        log.info("Item added to cart for user: {}", user.getEmail());

        return mapToResponse(cart);
    }

    /**
     * Update cart item quantity.
     */
    @Transactional
    public CartResponse updateItemQuantity(Long itemId, int quantity) {
        User user = userService.getCurrentUser();
        Cart cart = getOrCreateCart(user);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (quantity <= 0) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        } else {
            if (quantity > item.getProduct().getStockQuantity()) {
                throw new BadRequestException("Quantity exceeds available stock");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        cart = cartRepository.save(cart);
        log.info("Cart item updated for user: {}", user.getEmail());

        return mapToResponse(cart);
    }

    /**
     * Remove item from cart.
     */
    @Transactional
    public CartResponse removeItem(Long itemId) {
        User user = userService.getCurrentUser();
        Cart cart = getOrCreateCart(user);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        cart.removeItem(item);
        cartItemRepository.delete(item);
        cart = cartRepository.save(cart);

        log.info("Item removed from cart for user: {}", user.getEmail());
        return mapToResponse(cart);
    }

    /**
     * Clear all items from cart.
     */
    @Transactional
    public void clearCart() {
        User user = userService.getCurrentUser();
        Cart cart = getOrCreateCart(user);
        
        cart.clear();
        cartRepository.save(cart);
        
        log.info("Cart cleared for user: {}", user.getEmail());
    }

    /**
     * Get or create cart for user.
     */
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserIdWithItems(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .items(cart.getItems().stream()
                        .map(this::mapItemToResponse)
                        .collect(Collectors.toList()))
                .totalItems(cart.getTotalItems())
                .totalPrice(cart.getTotalPrice())
                .build();
    }

    private CartItemResponse mapItemToResponse(CartItem item) {
        Product product = item.getProduct();
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImage(product.getImageUrls().isEmpty() ? null : product.getImageUrls().get(0))
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .inStock(product.isInStock())
                .availableStock(product.getStockQuantity())
                .build();
    }
}
