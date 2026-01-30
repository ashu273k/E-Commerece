package com.ashu.E_Commerece.dto.order;

import com.ashu.E_Commerece.model.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for order creation request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotNull(message = "Shipping address is required")
    @Valid
    private Address shippingAddress;

    @Valid
    private Address billingAddress;

    private String paymentMethod;

    private String notes;
}
