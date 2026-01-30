package com.ashu.E_Commerece.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String paymentId;
    private Long orderId;
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private String message;
    private LocalDateTime processedAt;
}
