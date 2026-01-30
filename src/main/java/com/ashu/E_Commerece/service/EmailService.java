package com.ashu.E_Commerece.service;

import com.ashu.E_Commerece.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock email service that logs emails instead of sending them.
 */
@Service
@Slf4j
public class EmailService {

    public void sendOrderConfirmation(String to, Order order) {
        log.info("=== MOCK EMAIL: Order Confirmation ===");
        log.info("To: {}", to);
        log.info("Subject: Order Confirmation - {}", order.getOrderNumber());
        log.info("Total Amount: ${}", order.getTotalAmount());
        log.info("========================================");
    }

    public void sendOrderStatusUpdate(String to, Order order) {
        log.info("=== MOCK EMAIL: Order Status Update ===");
        log.info("To: {}", to);
        log.info("Order: {} - Status: {}", order.getOrderNumber(), order.getStatus());
        log.info("========================================");
    }

    public void sendWelcomeEmail(String to, String firstName) {
        log.info("=== MOCK EMAIL: Welcome {} to {} ===", firstName, to);
    }

    public void sendPasswordResetEmail(String to, String resetToken) {
        log.info("=== MOCK EMAIL: Password Reset to {} ===", to);
    }

    public void sendNotification(String to, String subject, String message) {
        log.info("=== MOCK EMAIL: {} to {} ===", subject, to);
    }
}
