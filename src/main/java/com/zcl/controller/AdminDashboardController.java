package com.zcl.controller;

import com.zcl.dto.AdminDashboardVO;
import com.zcl.dto.Result;
import com.zcl.dto.SalesStatsVO;
import com.zcl.dto.TopProductVO;
import com.zcl.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Result<AdminDashboardVO>> getStats() {
        return ResponseEntity.ok(Result.success("获取成功", dashboardService.getDashboardStats()));
    }

    @GetMapping("/sales")
    public ResponseEntity<Result<List<SalesStatsVO>>> getSalesStats(
            @RequestParam(defaultValue = "daily") String period) {
        return ResponseEntity.ok(Result.success("获取成功", dashboardService.getSalesStats(period)));
    }

    @GetMapping("/top-products")
    public ResponseEntity<Result<List<TopProductVO>>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(Result.success("获取成功", dashboardService.getTopProducts(limit)));
    }

    @GetMapping("/order-status")
    public ResponseEntity<Result<Map<Integer, Long>>> getOrderStatusDistribution() {
        return ResponseEntity.ok(Result.success("获取成功", dashboardService.getOrderStatusDistribution()));
    }
}
