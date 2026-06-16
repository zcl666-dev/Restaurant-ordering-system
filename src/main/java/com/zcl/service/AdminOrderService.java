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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductCategoryDao productCategoryDao;

    @Autowired
    private ProductOptionRelationDao productOptionRelationDao;

    @Autowired
    private OptionGroupDao optionGroupDao;

    @Autowired
    private OptionValueDao optionValueDao;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WxSubscribeService wxSubscribeService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductDetailService productDetailService;

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

        // keyword 同时搜索订单号和桌号
        if (status != null && start != null && end != null) {
            orders = orderDao.findByStatusAndDateRangeWithPaging(status, start, end, offset, size);
            totalElements = orderDao.count();
        } else if (status != null) {
            orders = orderDao.findByStatusWithPaging(status, offset, size);
            totalElements = orderDao.countByStatus(status);
        } else if (start != null && end != null) {
            orders = orderDao.findByDateRangeWithPaging(start, end, offset, size);
            totalElements = orderDao.countBetween(start, end);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            orders = orderDao.findByKeywordWithPaging(keyword.trim(), offset, size);
            totalElements = orderDao.countByKeyword(keyword.trim());
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
            if (newStatus == 3) {
                // 改为已完成，发送用餐提醒
                log.info("触发用餐提醒发送: orderNo={}", order.getOrderNo());
                wxSubscribeService.sendMealRemindMessage(order);
            } else if (newStatus == 4) {
                // 任何状态 → 已取消，发送订单取消通知
                log.info("触发订单取消通知发送: orderNo={}", order.getOrderNo());
                wxSubscribeService.sendOrderCancelMessage(order);
            }
        } catch (Exception e) {
            log.error("发送订单通知失败: orderNo={}, oldStatus={}, newStatus={}", order.getOrderNo(), oldStatus, newStatus, e);
        }
    }

    /**
     * 查询 id > lastOrderId 的已支付新订单（用于轮询新订单提醒）
     */
    public List<AdminOrderVO> getNewPaidOrders(Long lastOrderId) {
        List<Orders> orders = orderDao.findPaidOrdersAfterId(lastOrderId);
        return orders.stream().map(this::toOrderVO).collect(Collectors.toList());
    }

    /**
     * 获取已支付订单中最大的 id（用于前端初始化 lastOrderId）
     */
    public Long getMaxPaidOrderId() {
        return orderDao.findMaxPaidOrderId();
    }

    /**
     * 餐厅开始制作
     */
    public void startProduction(Long orderId) {
        orderService.startProduction(orderId);
    }

    /**
     * 餐厅拒绝订单（退回余额）
     */
    public void rejectOrder(Long orderId) {
        orderService.rejectOrder(orderId);
    }

    /**
     * 餐厅完成制作
     */
    public void completeProduction(Long orderId) {
        orderService.completeProduction(orderId);
    }

    /**
     * 获取未处理订单数量（状态=1 待制作）
     */
    public long getUnprocessedCount() {
        return orderDao.countByOrderStatus(1);
    }

    /**
     * 获取代客点餐商品列表（按分类分组，含规格信息）
     */
    public List<CategoryWithProductsVO> getProductsForStaffOrder() {
        // 查询所有启用的分类
        List<ProductCategory> categories = productCategoryDao.findAllOrderBySortOrder();
        categories = categories.stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 1)
                .collect(Collectors.toList());

        // 查询所有上架商品
        List<Product> allProducts = productDao.findAll();
        List<Product> products = allProducts.stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .collect(Collectors.toList());

        // 按分类分组
        var productsByCategory = products.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory().getId()));

        List<CategoryWithProductsVO> result = new ArrayList<>();
        for (ProductCategory category : categories) {
            CategoryWithProductsVO categoryVO = new CategoryWithProductsVO();
            categoryVO.setId(category.getId());
            categoryVO.setCategoryName(category.getCategoryName());
            categoryVO.setIcon(category.getIcon());

            List<Product> categoryProducts = productsByCategory.getOrDefault(category.getId(), new ArrayList<>());
            List<ProductVO> productVOList = categoryProducts.stream()
                    .map(p -> {
                        ProductVO vo = new ProductVO();
                        vo.setId(p.getId());
                        vo.setProductName(p.getProductName());
                        vo.setProductImage(p.getProductImage());
                        vo.setDesc(p.getDescription());
                        vo.setPrice(p.getPrice());
                        vo.setSalesCount(p.getSalesCount() != null ? p.getSalesCount() : 0);
                        vo.setStock(p.getStock());
                        vo.setHasOptions(p.getHasOptions());
                        return vo;
                    })
                    .collect(Collectors.toList());

            categoryVO.setProducts(productVOList);
            result.add(categoryVO);
        }
        return result;
    }

    /**
     * 代客点餐 - 获取商品详情（含规格信息）
     */
    public ProductDetailVO getStaffProductDetail(Long productId) {
        return productDetailService.getProductDetail(productId);
    }

    /**
     * 代客点餐 - 创建订单（直接进入待制作状态，不花费余额，不加积分）
     */
    public Long staffCreateOrder(StaffOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("请选择商品");
        }

        // 确定用户
        User user;
        if (request.getUserId() != null) {
            user = userDao.findById(request.getUserId());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
        } else {
            // 使用默认用户（id=1，堂食顾客）
            user = userDao.findById(1L);
            if (user == null) {
                throw new RuntimeException("系统默认用户不存在");
            }
        }

        // 校验所有商品
        List<StaffOrderRequest.OrderItem> requestItems = request.getItems();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (StaffOrderRequest.OrderItem item : requestItems) {
            Product product = productDao.findById(item.getProductId());
            if (product == null || product.getStatus() != 1) {
                throw new RuntimeException("商品不存在或已下架");
            }
            if (item.getQuantity() <= 0) {
                throw new RuntimeException("商品数量必须大于0");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品【" + product.getProductName() + "】库存不足");
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 扣减库存
        for (StaffOrderRequest.OrderItem item : requestItems) {
            Product product = productDao.findById(item.getProductId());
            product.setStock(product.getStock() - item.getQuantity());
            productDao.save(product);
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        Orders order = new Orders();
        order.setOrderNo(orderNo);
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        order.setPointsEarned(0); // 不加积分
        order.setOrderStatus(1); // 直接进入待制作
        order.setPaymentStatus(1); // 已支付
        order.setPaymentMethod("代客下单");
        order.setPaymentTime(LocalDateTime.now());
        order.setDiningType(request.getDiningType());
        order.setTableNumber(request.getTableNumber());
        order.setRemark(request.getRemark());
        orderDao.save(order);

        // 创建订单明细
        for (StaffOrderRequest.OrderItem item : requestItems) {
            Product product = productDao.findById(item.getProductId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductNameSnapshot(product.getProductName());
            orderItem.setProductImageSnapshot(product.getProductImage());
            orderItem.setOptionSnapshot(item.getOptionSnapshot());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotalAmount(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemDao.save(orderItem);
        }

        log.info("代客点餐订单创建成功: orderNo={}, userId={}, totalAmount={}", orderNo, user.getId(), totalAmount);
        return order.getId();
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%06d", new Random().nextInt(1000000));
        return "ORD" + datePart + randomPart;
    }

    private AdminOrderVO toOrderVO(Orders order) {
        List<OrderItem> items = orderItemDao.findByOrder(order);
        String displayName = "代客下单".equals(order.getPaymentMethod())
                ? "前台代客" : order.getUser().getNickName();
        return AdminOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUser().getId())
                .userNickName(displayName)
                .payAmount(order.getPayAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .diningType(order.getDiningType())
                .tableNumber(order.getTableNumber())
                .itemCount(items.size())
                .cancelDeadline(order.getCancelDeadline())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private AdminOrderDetailVO toDetailVO(Orders order) {
        List<OrderItem> items = orderItemDao.findByOrder(order);
        String displayName = "代客下单".equals(order.getPaymentMethod())
                ? "前台代客" : order.getUser().getNickName();
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
                .userNickName(displayName)
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
