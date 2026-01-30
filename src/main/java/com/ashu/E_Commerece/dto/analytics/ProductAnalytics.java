package com.ashu.E_Commerece.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for product analytics data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAnalytics {

    private long totalProducts;
    private long activeProducts;
    private long outOfStockProducts;
    private Map<String, Long> productsByCategory;
    private List<TopProduct> topRatedProducts;
    private List<TopProduct> topSellingProducts;
    private List<LowStockProduct> lowStockProducts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProduct {
        private Long id;
        private String name;
        private BigDecimal rating;
        private long salesCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LowStockProduct {
        private Long id;
        private String name;
        private int stockQuantity;
    }
}
