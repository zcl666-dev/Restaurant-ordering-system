package com.zcl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcl.dao.OrderDao;
import com.zcl.dao.OrderItemDao;
import com.zcl.dao.UserDao;
import com.zcl.dto.*;
import com.zcl.entity.OrderItem;
import com.zcl.entity.Orders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminOrderService {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderService.class);

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private UserDao userDao;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WxSubscribeService wxSubscribeService;

    public PageResult<AdminOrderVO> getOrderList(int page, int size, Integer status, String keyword,
                                                   String startDate, String endDate) {
        int offset = page * size;
        List<Orders> orders;
        long totalElements;

        LocalDateTime start = null;
        LocalDateTime end = null;
        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDateTime.parse(startDate + "T00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDateTime.parse(endDate + "T23:59:59");
        }

        if (status != null && start != null && end != null) {
            orders = orderDao.findByStatusAndDateRangeWithPaging(status, start, end, offset, size);
            totalElements = orderDao.count(); // 简化处理
        } else if (status != null) {
            orders = orderDao.findByStatusWithPaging(status, offset, size);
            totalElements = orderDao.countByStatus(status);
        } else if (start != null && end != null) {
            orders = orderDao.findByDateRangeWithPaging(start, end, offset, size);
            totalElements = orderDao.countBetween(start, end);
        } else {
            orders = orderDao.findAllWithPaging(offset, size);
            totalElements = orderDao.count();
        }

        List<AdminOrderVO> content = orders.stream().map(this::toOrderVO).collect(Collectors.toList());

        return PageResult.<AdminOrderVO>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / size))
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    public AdminOrderDetailVO getOrderDetail(Long id) {
        Orders order = orderDao.findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return toDetailVO(order);
    }

    public void updateOrderStatus(Long id, Integer newStatus) {
        Orders order = orderDao.findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        Integer oldStatus = order.getOrderStatus();
        log.info("管理员更新订单状态: orderNo={}, oldStatus={}, newStatus={}", order.getOrderNo(), oldStatus, newStatus);

        order.setOrderStatus(newStatus);
        orderDao.save(order);

        // 根据状态变化发送相应的通知
        try {
            if (newStatus == 4) {
                // 改为已完成，发送用餐提醒（不限制旧状态）
                log.info("触发用餐提醒发送: orderNo={}", order.getOrderNo());
                wxSubscribeService.sendMealRemindMessage(order);
            } else if (newStatus == 5) {
                // 任何状态 → 已取消，发送订单取消通知
                log.info("触发订单取消通知发送: orderNo={}", order.getOrderNo());
                wxSubscribeService.sendOrderCancelMessage(order);
            }
        } catch (Exception e) {
            log.error("发送订单通知失败: orderNo={}, oldStatus={}, newStatus={}", order.getOrderNo(), oldStatus, newStatus, e);
        }
    }

    private AdminOrderVO toOrderVO(Orders order) {
        List<OrderItem> items = orderItemDao.findByOrder(order);
        return AdminOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUser().getId())
                .userNickName(order.getUser().getNickName())
                .payAmount(order.getPayAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .diningType(order.getDiningType())
                .itemCount(items.size())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private AdminOrderDetailVO toDetailVO(Orders order) {
        List<OrderItem> items = orderItemDao.findByOrder(order);
        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            List<OptionVO> options = new ArrayList<>();
            if (item.getOptionSnapshot() != null) {
                try {
                    options = objectMapper.readValue(item.getOptionSnapshot(), new TypeReference<>() {});
                } catch (Exception ignored) {}
            }
            return OrderItemVO.builder()
                    .productName(item.getProductNameSnapshot())
                    .productImage(item.getProductImageSnapshot())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotalAmount(item.getSubtotalAmount())
                    .options(options)
                    .build();
        }).collect(Collectors.toList());

        return AdminOrderDetailVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUser().getId())
                .userNickName(order.getUser().getNickName())
                .userAvatarUrl(order.getUser().getAvatarUrl())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .payAmount(order.getPayAmount())
                .pointsEarned(order.getPointsEarned())
                .diningType(order.getDiningType())
                .tableNumber(order.getTableNumber())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentTime(order.getPaymentTime())
                .remark(order.getRemark())
                .createdAt(order.getCreatedAt())
                .items(itemVOs)
                .build();
    }
}
