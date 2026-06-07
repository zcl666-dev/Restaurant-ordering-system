package com.zcl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcl.dao.*;
import com.zcl.dto.*;
import com.zcl.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private CartDao cartDao;

    @Autowired
    private CartItemDao cartItemDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private OptionGroupDao optionGroupDao;

    @Autowired
    private OptionValueDao optionValueDao;

    @Autowired
    private WxSubscribeService wxSubscribeService;

    @Autowired
    private PointLogDao pointLogDao;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public OrderCreateResponse createOrder(Long userId, Integer diningType, String tableNumber, String remark) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Cart cart = cartDao.findByUser(user);
        if (cart == null) {
            throw new RuntimeException("购物车为空");
        }

        List<CartItem> cartItems = cartItemDao.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车中没有商品");
        }

        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            if (product == null || product.getStatus() != 1) {
                throw new RuntimeException("商品【" + ci.getProductNameSnapshot() + "】已下架");
            }
            if (ci.getQuantity() > product.getStock()) {
                throw new RuntimeException("商品【" + ci.getProductNameSnapshot() + "】库存不足");
            }
        }

        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            product.setStock(product.getStock() - ci.getQuantity());
            productDao.save(product);
        }

        String orderNo = generateOrderNo();

        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUser(user);
        order.setCart(cart);
        order.setTotalAmount(cart.getTotalAmount());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(cart.getTotalAmount());
        order.setPointsEarned(0);
        order.setOrderStatus(0);
        order.setPaymentStatus(0);
        order.setRemark(remark);
        order.setDiningType(String.valueOf(diningType));
        order.setTableNumber(tableNumber);
        orderDao.save(order);

        for (CartItem ci : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(ci.getProduct());
            orderItem.setProductNameSnapshot(ci.getProductNameSnapshot());
            orderItem.setProductImageSnapshot(ci.getProductImageSnapshot());
            orderItem.setOptionSnapshot(ci.getOptionSnapshot());
            orderItem.setQuantity(ci.getQuantity());
            orderItem.setUnitPrice(ci.getUnitPrice());
            orderItem.setSubtotalAmount(ci.getSubtotalAmount());
            orderItemDao.save(orderItem);
        }

        // 清空购物车
        cartItemDao.deleteByCart(cart);
        cart.setCartStatus("active");
        cart.setTotalQuantity(0);
        cart.setTotalAmount(BigDecimal.ZERO);
        cartDao.save(cart);

        log.info("订单创建成功: orderNo={}, userId={}, payAmount={}", orderNo, userId, order.getPayAmount());

        return OrderCreateResponse.builder().orderId(order.getId()).build();
    }

    public OrderDetailVO getOrderDetail(Long userId, Long orderId) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Orders order = orderDao.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }

        List<OrderItem> items = orderItemDao.findByOrder(order);
        List<OrderItemVO> itemVOs = items.stream()
                .map(this::toOrderItemVO)
                .collect(Collectors.toList());

        return OrderDetailVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .payAmount(order.getPayAmount())
                .diningType(order.getDiningType())
                .tableNumber(order.getTableNumber())
                .remark(order.getRemark())
                .createdAt(order.getCreatedAt())
                .items(itemVOs)
                .build();
    }

    public void cancelOrder(Long userId, Long orderId) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Orders order = orderDao.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("只有待支付订单才能取消");
        }

        List<OrderItem> items = orderItemDao.findByOrder(order);
        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productDao.save(product);
            }
        }

        order.setOrderStatus(5);
        orderDao.save(order);

        // 发送订单取消通知
        try {
            wxSubscribeService.sendOrderCancelMessage(order);
        } catch (Exception e) {
            log.error("发送订单取消通知失败: orderNo={}", order.getOrderNo(), e);
        }

        log.info("订单已取消: orderNo={}", order.getOrderNo());
    }

    public void updateDiningType(Long userId, Long orderId, Integer diningType) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Orders order = orderDao.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("只有待支付订单才能修改就餐方式");
        }

        order.setDiningType(String.valueOf(diningType));
        orderDao.save(order);

        log.info("更新就餐方式: orderId={}, diningType={}", orderId, diningType);
    }

    public void completeOrder(Long userId, Long orderId) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Orders order = orderDao.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getOrderStatus() == 5) {
            throw new RuntimeException("订单已取消，无法完成");
        }

        if (order.getOrderStatus() == 4) {
            throw new RuntimeException("订单已完成");
        }

        // 只有制作中(2)或待取餐(3)的状态才能完成
        if (order.getOrderStatus() != 2 && order.getOrderStatus() != 3) {
            throw new RuntimeException("当前订单状态无法完成");
        }

        order.setOrderStatus(4);
        orderDao.save(order);

        // 发送用餐提醒通知
        try {
            wxSubscribeService.sendMealRemindMessage(order);
        } catch (Exception e) {
            log.error("发送用餐提醒通知失败: orderNo={}", order.getOrderNo(), e);
        }

        log.info("订单已完成: orderNo={}", order.getOrderNo());
    }

    public void payOrder(Long userId, Long orderId) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Orders order = orderDao.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getOrderStatus() == 5) {
            throw new RuntimeException("订单已取消，无法支付");
        }

        if (order.getPaymentStatus() == 1) {
            throw new RuntimeException("订单已支付");
        }

        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("当前订单状态无法支付");
        }

        User user = order.getUser();

        if (user.getBalance().compareTo(order.getPayAmount()) < 0) {
            throw new RuntimeException("余额不足");
        }

        user.setBalance(user.getBalance().subtract(order.getPayAmount()));

        int pointsEarned = order.getPayAmount().intValue();
        user.setPointsBalance(user.getPointsBalance() + pointsEarned);

        user.setTotalSpentAmount(user.getTotalSpentAmount().add(order.getPayAmount()));
        user.setTotalOrderCount(user.getTotalOrderCount() + 1);
        userDao.save(user);

        order.setPaymentStatus(1);
        order.setOrderStatus(2);
        order.setPaymentTime(LocalDateTime.now());
        order.setPointsEarned(pointsEarned);
        order.setPaymentMethod("余额支付");
        orderDao.save(order);

        // 记录积分流水
        if (pointsEarned > 0) {
            PointLog pointLog = new PointLog();
            pointLog.setUser(user);
            pointLog.setOrder(order);
            pointLog.setType(1); // 1=获得
            pointLog.setPointsChange(pointsEarned);
            pointLog.setBalanceAfter(user.getPointsBalance());
            pointLog.setRemark("订单支付获得积分");
            pointLogDao.save(pointLog);
        }

        // 发送下单成功通知
        try {
            wxSubscribeService.sendOrderFinishMessage(order);
        } catch (Exception e) {
            log.error("发送下单成功通知失败: orderNo={}", order.getOrderNo(), e);
        }

        log.info("订单支付成功: orderNo={}, payAmount={}, pointsEarned={}", order.getOrderNo(), order.getPayAmount(), pointsEarned);
    }

    public List<OrderListVO> getOrderList(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<Orders> orders = orderDao.findByUserId(userId);

        return orders.stream()
                .map(o -> {
                    List<OrderItem> items = orderItemDao.findByOrder(o);
                    List<OrderItemVO> itemVOs = items.stream()
                            .map(this::toOrderItemVO)
                            .collect(Collectors.toList());

                    return OrderListVO.builder()
                            .id(o.getId())
                            .orderNo(o.getOrderNo())
                            .payAmount(o.getPayAmount())
                            .orderStatus(o.getOrderStatus())
                            .diningType(o.getDiningType())
                            .tableNumber(o.getTableNumber())
                            .itemCount(items.size())
                            .createdAt(o.getCreatedAt())
                            .items(itemVOs)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%06d", new Random().nextInt(1000000));
        return "ORD" + datePart + randomPart;
    }

    private OrderItemVO toOrderItemVO(OrderItem item) {
        List<OptionVO> options = parseOptionSnapshot(item.getOptionSnapshot());

        return OrderItemVO.builder()
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductNameSnapshot())
                .productImage(item.getProductImageSnapshot())
                .optionSnapshot(item.getOptionSnapshot())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotalAmount(item.getSubtotalAmount())
                .options(options)
                .build();
    }

    private List<OptionVO> parseOptionSnapshot(String optionSnapshot) {
        if (optionSnapshot == null || optionSnapshot.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<OptionVO> optionList = objectMapper.readValue(
                    optionSnapshot,
                    new TypeReference<List<OptionVO>>() {}
            );
            return optionList.stream()
                    .map(vo -> {
                        OptionGroup group = optionGroupDao.findById(vo.getGroupId());
                        OptionValue value = optionValueDao.findById(vo.getOptionId());
                        return OptionVO.builder()
                                .groupId(vo.getGroupId())
                                .groupName(group != null ? group.getGroupName() : "")
                                .optionId(vo.getOptionId())
                                .valueName(value != null ? value.getValueName() : "")
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("规格快照解析失败, optionSnapshot={}", optionSnapshot, e);
            return Collections.emptyList();
        }
    }
}
