package com.ashu.E_Commerece.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for user analytics data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnalytics {

    private long totalUsers;
    private long activeUsers;
    private long newUsersThisMonth;
    private Map<String, Long> usersByRole;
}
