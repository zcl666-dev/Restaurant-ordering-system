package com.zcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardVO {
    private long totalUsers;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long todayOrders;
    private BigDecimal todayRevenue;
    private long pendingOrders;
    private long productCount;
}
