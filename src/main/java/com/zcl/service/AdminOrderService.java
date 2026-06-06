package com.zcl.service;

import com.zcl.dto.AdminOrderDetailVO;
import com.zcl.dto.AdminOrderVO;
import com.zcl.dto.OptionVO;
import com.zcl.dto.OrderItemVO;
import com.zcl.dto.PageResult;
import com.zcl.entity.OrderItem;
import com.zcl.entity.Orders;
import com.zcl.repository.OrderItemRepository;
import com.zcl.repository.OrderRepository;
import com.zcl.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminOrderService {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final WxSubscribeService wxSubscribeService;

    public AdminOrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                              UserRepository userRepository, ObjectMapper objectMapper,
                              WxSubscribeService wxSubscribeService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.wxSubscribeService = wxSubscribeService;
    }

    public PageResult<AdminOrderVO> getOrderList(int page, int size, Integer status, String keyword,
                                                   String startDate, String endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Orders> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("orderStatus"), status));
            }
            if (startDate != null && !startDate.isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        LocalDateTime.parse(startDate + "T00:00:00")));
            }
            if (endDate != null && !endDate.isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                        LocalDateTime.parse(endDate + "T23:59:59")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Orders> orderPage = orderRepository.findAll(spec, pageable);

        return PageResult.<AdminOrderVO>builder()
                .content(orderPage.getContent().stream().map(this::toOrderVO).toList())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    public AdminOrderDetailVO getOrderDetail(Long id) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        return toDetailVO(order);
    }

    public void updateOrderStatus(Long id, Integer newStatus) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Integer oldStatus = order.getOrderStatus();
        log.info("管理员更新订单状态: orderNo={}, oldStatus={}, newStatus={}", order.getOrderNo(), oldStatus, newStatus);

        order.setOrderStatus(newStatus);
        orderRepository.save(order);

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
        List<OrderItem> items = orderItemRepository.findByOrder(order);
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
        List<OrderItem> items = orderItemRepository.findByOrder(order);
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
        }).toList();

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
