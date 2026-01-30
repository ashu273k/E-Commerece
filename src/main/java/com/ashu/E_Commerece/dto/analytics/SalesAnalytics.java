package com.ashu.E_Commerece.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for sales analytics data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesAnalytics {

    private BigDecimal totalRevenue;
    private long totalOrders;
    private BigDecimal averageOrderValue;
    private Map<String, Long> ordersByStatus;
    private List<DailySales> dailySales;
    private List<MonthlySales> monthlySales;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailySales {
        private String date;
        private BigDecimal revenue;
        private long orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlySales {
        private String month;
        private BigDecimal revenue;
        private long orderCount;
    }
}
