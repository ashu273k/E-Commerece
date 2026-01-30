package com.ashu.E_Commerece.service;

import com.ashu.E_Commerece.dto.analytics.ProductAnalytics;
import com.ashu.E_Commerece.dto.analytics.SalesAnalytics;
import com.ashu.E_Commerece.dto.analytics.UserAnalytics;
import com.ashu.E_Commerece.model.Role;
import com.ashu.E_Commerece.repository.OrderRepository;
import com.ashu.E_Commerece.repository.ProductRepository;
import com.ashu.E_Commerece.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SalesAnalytics getSalesAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        long totalOrders = orderRepository.countTotalOrders();
        BigDecimal avgOrderValue = orderRepository.calculateAverageOrderValue();

        Map<String, Long> ordersByStatus = new HashMap<>();
        orderRepository.countOrdersByStatus().forEach(row -> 
            ordersByStatus.put(row[0].toString(), ((Number) row[1]).longValue()));

        return SalesAnalytics.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue != null ? avgOrderValue : BigDecimal.ZERO)
                .ordersByStatus(ordersByStatus)
                .dailySales(new ArrayList<>())
                .monthlySales(new ArrayList<>())
                .build();
    }

    @Transactional(readOnly = true)
    public ProductAnalytics getProductAnalytics() {
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countActiveProducts();
        long outOfStock = productRepository.findLowStockProducts(1).size();

        Map<String, Long> productsByCategory = new HashMap<>();
        productRepository.countProductsByCategory().forEach(row -> 
            productsByCategory.put(row[0] != null ? row[0].toString() : "Uncategorized", 
                                   ((Number) row[1]).longValue()));

        List<ProductAnalytics.TopProduct> topRated = productRepository
            .findTop10ByActiveTrueOrderByAverageRatingDesc().stream()
            .map(p -> ProductAnalytics.TopProduct.builder()
                .id(p.getId()).name(p.getName()).rating(p.getAverageRating()).build())
            .collect(Collectors.toList());

        List<ProductAnalytics.LowStockProduct> lowStock = productRepository
            .findLowStockProducts(10).stream()
            .map(p -> ProductAnalytics.LowStockProduct.builder()
                .id(p.getId()).name(p.getName()).stockQuantity(p.getStockQuantity()).build())
            .collect(Collectors.toList());

        return ProductAnalytics.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .outOfStockProducts(outOfStock)
                .productsByCategory(productsByCategory)
                .topRatedProducts(topRated)
                .lowStockProducts(lowStock)
                .build();
    }

    @Transactional(readOnly = true)
    public UserAnalytics getUserAnalytics() {
        long totalUsers = userRepository.count();
        
        Map<String, Long> usersByRole = new HashMap<>();
        usersByRole.put("USER", userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.USER).count());
        usersByRole.put("ADMIN", userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.ADMIN).count());

        return UserAnalytics.builder()
                .totalUsers(totalUsers)
                .activeUsers(totalUsers)
                .newUsersThisMonth(0L)
                .usersByRole(usersByRole)
                .build();
    }
}
