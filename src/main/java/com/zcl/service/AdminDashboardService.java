package com.zcl.service;

import com.zcl.dao.OrderDao;
import com.zcl.dao.OrderItemDao;
import com.zcl.dao.ProductDao;
import com.zcl.dao.UserDao;
import com.zcl.dto.AdminDashboardVO;
import com.zcl.dto.SalesStatsVO;
import com.zcl.dto.TopProductVO;
import com.zcl.entity.Orders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private OrderItemDao orderItemDao;

    public AdminDashboardVO getDashboardStats() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return AdminDashboardVO.builder()
                .totalUsers(userDao.count())
                .totalOrders(orderDao.count())
                .totalRevenue(orderDao.sumTotalRevenue())
                .todayOrders(orderDao.countBetween(todayStart, todayEnd))
                .todayRevenue(orderDao.sumRevenueBetween(todayStart, todayEnd))
                .pendingOrders(orderDao.countByOrderStatus(0))
                .productCount(productDao.count())
                .build();
    }

    public List<SalesStatsVO> getSalesStats(String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        DateTimeFormatter formatter;

        switch (period == null ? "daily" : period) {
            case "monthly":
                startDate = endDate.minusMonths(12);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            case "weekly":
                startDate = endDate.minusWeeks(12);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
            default: // daily
                startDate = endDate.minusDays(29);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
        }

        LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);

        List<Orders> paidOrders = orderDao.findPaidOrdersBetween(start, end);

        // 按日期分组统计
        Map<String, SalesStatsVO> statsMap = new HashMap<>();
        if ("monthly".equals(period)) {
            for (Orders order : paidOrders) {
                String key = order.getCreatedAt().format(formatter);
                statsMap.computeIfAbsent(key, k -> SalesStatsVO.builder()
                        .date(k).orderCount(0).revenue(BigDecimal.ZERO).build());
                SalesStatsVO vo = statsMap.get(key);
                vo.setOrderCount(vo.getOrderCount() + 1);
                vo.setRevenue(vo.getRevenue().add(order.getPayAmount()));
            }
        } else {
            for (Orders order : paidOrders) {
                String key = order.getCreatedAt().toLocalDate().format(formatter);
                statsMap.computeIfAbsent(key, k -> SalesStatsVO.builder()
                        .date(k).orderCount(0).revenue(BigDecimal.ZERO).build());
                SalesStatsVO vo = statsMap.get(key);
                vo.setOrderCount(vo.getOrderCount() + 1);
                vo.setRevenue(vo.getRevenue().add(order.getPayAmount()));
            }
        }

        // 填充空日期
        List<SalesStatsVO> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String key;
            if ("monthly".equals(period)) {
                key = current.withDayOfMonth(1).format(formatter);
                if (!statsMap.containsKey(key)) {
                    result.add(SalesStatsVO.builder().date(key).orderCount(0).revenue(BigDecimal.ZERO).build());
                } else if (result.stream().noneMatch(v -> v.getDate().equals(key))) {
                    result.add(statsMap.get(key));
                }
                current = current.plusMonths(1);
            } else {
                key = current.format(formatter);
                result.add(statsMap.getOrDefault(key,
                        SalesStatsVO.builder().date(key).orderCount(0).revenue(BigDecimal.ZERO).build()));
                current = current.plusDays(1);
            }
        }

        return result;
    }

    public List<TopProductVO> getTopProducts(int limit) {
        List<Object[]> rows = orderItemDao.findTopProducts(limit);
        List<TopProductVO> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(TopProductVO.builder()
                    .productId((Long) row[0])
                    .productName((String) row[1])
                    .totalQuantity((Long) row[2])
                    .totalRevenue((BigDecimal) row[3])
                    .build());
        }
        return result;
    }

    public Map<Integer, Long> getOrderStatusDistribution() {
        List<Object[]> rows = orderDao.countGroupByStatus();
        Map<Integer, Long> distribution = new HashMap<>();
        for (Object[] row : rows) {
            distribution.put((Integer) row[0], (Long) row[1]);
        }
        return distribution;
    }
}
