package com.ashu.E_Commerece.service;

import com.ashu.E_Commerece.dto.payment.PaymentRequest;
import com.ashu.E_Commerece.dto.payment.PaymentResponse;
import com.ashu.E_Commerece.exception.BadRequestException;
import com.ashu.E_Commerece.exception.ResourceNotFoundException;
import com.ashu.E_Commerece.model.Order;
import com.ashu.E_Commerece.model.OrderStatus;
import com.ashu.E_Commerece.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in pending status");
        }

        if (order.getTotalAmount().compareTo(request.getAmount()) != 0) {
            throw new BadRequestException("Payment amount does not match order total");
        }

        // Mock payment processing
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("=== MOCK PAYMENT PROCESSING ===");
        log.info("Payment ID: {}", paymentId);
        log.info("Order: {}", order.getOrderNumber());
        log.info("Amount: ${}", request.getAmount());
        log.info("Method: {}", request.getPaymentMethod());
        log.info("Status: SUCCESS");
        log.info("================================");

        order.setPaymentId(paymentId);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return PaymentResponse.builder()
                .paymentId(paymentId)
                .orderId(order.getId())
                .amount(request.getAmount())
                .status("SUCCESS")
                .paymentMethod(request.getPaymentMethod())
                .message("Payment processed successfully (MOCK)")
                .processedAt(LocalDateTime.now())
                .build();
    }

    public PaymentResponse getPaymentStatus(String paymentId) {
        Order order = orderRepository.findAll().stream()
                .filter(o -> paymentId.equals(o.getPaymentId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        return PaymentResponse.builder()
                .paymentId(paymentId)
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .status("SUCCESS")
                .paymentMethod(order.getPaymentMethod())
                .message("Payment completed")
                .processedAt(order.getUpdatedAt())
                .build();
    }
}
