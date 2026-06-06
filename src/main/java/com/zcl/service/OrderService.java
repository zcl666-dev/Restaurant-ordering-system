package com.zcl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcl.dto.OptionVO;
import com.zcl.dto.OrderCreateResponse;
import com.zcl.dto.OrderDetailVO;
import com.zcl.dto.OrderItemVO;
import com.zcl.dto.OrderListVO;
import com.zcl.entity.Cart;
import com.zcl.entity.CartItem;
import com.zcl.entity.OptionGroup;
import com.zcl.entity.OptionValue;
import com.zcl.entity.OrderItem;
import com.zcl.entity.Orders;
import com.zcl.entity.Product;
import com.zcl.entity.User;
import com.zcl.repository.CartItemRepository;
import com.zcl.repository.CartRepository;
import com.zcl.repository.OptionGroupRepository;
import com.zcl.repository.OptionValueRepository;
import com.zcl.repository.OrderItemRepository;
import com.zcl.repository.OrderRepository;
import com.zcl.repository.ProductRepository;
import com.zcl.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private OptionGroupRepository optionGroupRepository;

    @Autowired
    private OptionValueRepository optionValueRepository;

    @Autowired
    private WxSubscribeService wxSubscribeService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResponse createOrder() {
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Cart cart = cartRepository.findByUserAndCartStatus(user, "active")
                .orElseThrow(() -> new RuntimeException("购物车为空"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
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
            productRepository.save(product);
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
        order.setRemark(cart.getRemark());
        order.setDiningType(cart.getDiningType());
        order.setTableNumber(cart.getTableNumber());
        order = orderRepository.save(order);

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
            orderItemRepository.save(orderItem);
        }

        cart.setCartStatus("converted");
        cartRepository.save(cart);

        log.info("订单创建成功: orderNo={}, userId={}, payAmount={}", orderNo, userId, order.getPayAmount());

        return OrderCreateResponse.builder().orderId(order.getId()).build();
    }

    public OrderDetailVO getOrderDetail(Long orderId) {
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }

        List<OrderItem> items = orderItemRepository.findByOrder(order);
        List<OrderItemVO> itemVOs = items.stream()
                .map(this::toOrderItemVO)
                .collect(Collectors.toList());

        return OrderDetailVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .payAmount(order.getPayAmount())
                .remark(order.getRemark())
                .createdAt(order.getCreatedAt())
                .items(itemVOs)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("只有待支付订单才能取消");
        }

        List<OrderItem> items = orderItemRepository.findByOrder(order);
        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setOrderStatus(5);
        orderRepository.save(order);

        // 发送订单取消通知
        try {
            wxSubscribeService.sendOrderCancelMessage(order);
        } catch (Exception e) {
            log.error("发送订单取消通知失败: orderNo={}", order.getOrderNo(), e);
        }

        log.info("订单已取消: orderNo={}", order.getOrderNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) {
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

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
        orderRepository.save(order);

        // 发送用餐提醒通知
        try {
            wxSubscribeService.sendMealRemindMessage(order);
        } catch (Exception e) {
            log.error("发送用餐提醒通知失败: orderNo={}", order.getOrderNo(), e);
        }

        log.info("订单已完成: orderNo={}", order.getOrderNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId) {
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

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
        userRepository.save(user);

        order.setPaymentStatus(1);
        order.setOrderStatus(2);
        order.setPaymentTime(LocalDateTime.now());
        order.setPointsEarned(pointsEarned);
        order.setPaymentMethod("余额支付");
        orderRepository.save(order);

        // 发送下单成功通知
        try {
            wxSubscribeService.sendOrderFinishMessage(order);
        } catch (Exception e) {
            log.error("发送下单成功通知失败: orderNo={}", order.getOrderNo(), e);
        }

        log.info("订单支付成功: orderNo={}, payAmount={}, pointsEarned={}", order.getOrderNo(), order.getPayAmount(), pointsEarned);
    }

    public List<OrderListVO> getOrderList() {
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            return Collections.emptyList();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<Orders> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);

        return orders.stream()
                .map(o -> {
                    List<OrderItem> items = orderItemRepository.findByOrder(o);
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
                .productName(item.getProductNameSnapshot())
                .productImage(item.getProductImageSnapshot())
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
                        OptionGroup group = optionGroupRepository.findById(vo.getGroupId()).orElse(null);
                        OptionValue value = optionValueRepository.findById(vo.getOptionId()).orElse(null);
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