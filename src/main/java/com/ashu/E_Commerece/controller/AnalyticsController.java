package com.ashu.E_Commerece.controller;

import com.ashu.E_Commerece.dto.analytics.ProductAnalytics;
import com.ashu.E_Commerece.dto.analytics.SalesAnalytics;
import com.ashu.E_Commerece.dto.analytics.UserAnalytics;
import com.ashu.E_Commerece.dto.common.ApiResponse;
import com.ashu.E_Commerece.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics APIs (Admin only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/sales")
    @Operation(summary = "Get sales analytics")
    public ResponseEntity<ApiResponse<SalesAnalytics>> getSalesAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate == null) startDate = LocalDateTime.now().minusMonths(1);
        if (endDate == null) endDate = LocalDateTime.now();
        
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getSalesAnalytics(startDate, endDate)));
    }

    @GetMapping("/products")
    @Operation(summary = "Get product analytics")
    public ResponseEntity<ApiResponse<ProductAnalytics>> getProductAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getProductAnalytics()));
    }

    @GetMapping("/users")
    @Operation(summary = "Get user analytics")
    public ResponseEntity<ApiResponse<UserAnalytics>> getUserAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getUserAnalytics()));
    }
}
