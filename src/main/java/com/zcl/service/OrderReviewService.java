package com.zcl.service;

import com.zcl.dao.OrderDao;
import com.zcl.dao.OrderItemDao;
import com.zcl.dao.OrderReviewDao;
import com.zcl.dao.UserDao;
import com.zcl.entity.OrderItem;
import com.zcl.entity.OrderReview;
import com.zcl.entity.Orders;
import com.zcl.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class OrderReviewService {

    private static final Logger log = LoggerFactory.getLogger(OrderReviewService.class);

    @Autowired
    private OrderReviewDao orderReviewDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private UserDao userDao;

    /**
     * 提交评价
     */
    public void createReview(Long userId, Long orderId, Integer rating, String content) {
        // 校验评分
        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }

        // 校验订单
        Orders order = orderDao.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 校验订单归属
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权评价此订单");
        }

        // 校验订单状态（已完成）
        if (order.getOrderStatus() != 3) {
            throw new RuntimeException("只有已完成的订单才能评价");
        }

        // 校验是否已评价
        OrderReview existing = orderReviewDao.findByOrderId(orderId);
        if (existing != null) {
            throw new RuntimeException("该订单已评价");
        }

        // 校验用户
        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 保存评价
        OrderReview review = new OrderReview();
        review.setOrder(order);
        review.setUser(user);
        review.setRating(rating);
        review.setContent(content);
        orderReviewDao.save(review);

        log.info("用户{}评价了订单{}, 评分{}", userId, orderId, rating);
    }

    /**
     * 查询订单是否已评价
     */
    public Map<String, Object> getReviewByOrderId(Long orderId) {
        OrderReview review = orderReviewDao.findByOrderId(orderId);
        if (review == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", review.getId());
        map.put("rating", review.getRating());
        map.put("content", review.getContent());
        map.put("createdAt", review.getCreatedAt());
        return map;
    }

    /**
     * 后台分页查询评价列表
     */
    public Map<String, Object> getReviewList(Integer rating, int page, int size) {
        List<OrderReview> reviews;
        long total;

        if (rating != null) {
            reviews = orderReviewDao.findByRating(rating, page, size);
            total = orderReviewDao.countByRating(rating);
        } else {
            reviews = orderReviewDao.findAllWithPaging(page, size);
            total = orderReviewDao.countAll();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (OrderReview r : reviews) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("orderId", r.getOrder().getId());
            map.put("orderNo", r.getOrder().getOrderNo());
            map.put("orderAmount", r.getOrder().getPayAmount());
            map.put("userId", r.getUser().getId());
            map.put("userName", r.getUser().getNickName());
            map.put("userAvatar", r.getUser().getAvatarUrl());
            map.put("rating", r.getRating());
            map.put("content", r.getContent());
            map.put("createdAt", r.getCreatedAt());

            // 查询订单商品
            List<OrderItem> items = orderItemDao.findByOrderId(r.getOrder().getId());
            List<Map<String, Object>> itemList = new ArrayList<>();
            for (OrderItem item : items) {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("productName", item.getProductNameSnapshot());
                itemMap.put("productImage", item.getProductImageSnapshot());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unitPrice", item.getUnitPrice());
                itemMap.put("subtotal", item.getSubtotalAmount());
                itemList.add(itemMap);
            }
            map.put("items", itemList);

            list.add(map);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", list);
        result.put("totalElements", total);
        result.put("totalPages", (total + size - 1) / size);
        result.put("currentPage", page);
        result.put("pageSize", size);
        return result;
    }
}
