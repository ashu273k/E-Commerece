package com.ashu.E_Commerece.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for product search/filter criteria.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchCriteria {

    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private Boolean inStock;
    private Boolean featured;
    private BigDecimal minRating;
}
