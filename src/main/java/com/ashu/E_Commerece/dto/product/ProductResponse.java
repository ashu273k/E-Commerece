package com.ashu.E_Commerece.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for product response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal effectivePrice;
    private Integer stockQuantity;
    private String sku;
    private String brand;
    private List<String> imageUrls;
    private Long categoryId;
    private String categoryName;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private boolean active;
    private boolean featured;
    private boolean inStock;
    private LocalDateTime createdAt;
}
