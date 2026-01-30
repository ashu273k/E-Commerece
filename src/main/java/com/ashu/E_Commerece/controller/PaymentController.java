package com.ashu.E_Commerece.controller;

import com.ashu.E_Commerece.dto.common.ApiResponse;
import com.ashu.E_Commerece.dto.payment.PaymentRequest;
import com.ashu.E_Commerece.dto.payment.PaymentResponse;
import com.ashu.E_Commerece.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing APIs (Mock)")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @Operation(summary = "Process payment (Mock)")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment processed", response));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment status")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
