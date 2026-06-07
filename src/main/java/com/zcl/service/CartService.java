package com.zcl.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcl.dao.*;
import com.zcl.dto.*;
import com.zcl.entity.*;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务类
 */
@Service
@Transactional
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

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

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 加入购物车
     */
    public CartAddResponse addToCart(Long userId, CartAddRequest request) {
        // 1. 查询用户
        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 查询或创建购物车
        Cart cart = cartDao.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setCartStatus("active");
            cart.setTotalQuantity(0);
            cart.setTotalAmount(BigDecimal.ZERO);
            cartDao.save(cart);
        }

        // 3. 查询商品信息
        Product product = productDao.findById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 检查商品是否上架
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架");
        }

        // 处理规格快照
        String optionSnapshot = normalizeOptionSnapshot(request.getOptionSnapshot());
        log.info("加入购物车: productId={}, optionSnapshot={}", request.getProductId(), optionSnapshot);

        // 4. 查询购物车中是否已有相同商品
        List<CartItem> existingItems = cartItemDao.findByCart(cart);
        CartItem existingItem = null;
        for (CartItem ci : existingItems) {
            if (ci.getProduct() != null && ci.getProduct().getId().equals(product.getId())
                    && isSameOptionSnapshot(ci.getOptionSnapshot(), optionSnapshot)) {
                existingItem = ci;
                break;
            }
        }

        if (existingItem != null) {
            log.info("找到相同规格项, 执行数量+1");
            // 5. 已存在商品，数量 +1
            int newQuantity = existingItem.getQuantity() + 1;

            // 库存校验
            if (newQuantity > product.getStock()) {
                throw new RuntimeException("库存不足");
            }

            existingItem.setQuantity(newQuantity);
            existingItem.setSubtotalAmount(
                    existingItem.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity))
            );
            cartItemDao.save(existingItem);
        } else {
            // 6. 不存在商品，新增 cart_item
            int quantity = request.getQuantity() != null ? request.getQuantity() : 1;

            // 库存校验
            if (quantity > product.getStock()) {
                throw new RuntimeException("库存不足");
            }

            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setProductNameSnapshot(product.getProductName());
            newItem.setProductImageSnapshot(product.getProductImage());
            newItem.setOptionSnapshot(optionSnapshot);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(product.getPrice());
            newItem.setSubtotalAmount(
                    product.getPrice().multiply(BigDecimal.valueOf(quantity))
            );
            cartItemDao.save(newItem);
        }

        // 7. 更新购物车汇总
        updateCartSummary(cart);

        // 8. 返回结果
        return CartAddResponse.builder()
                .cartId(cart.getId())
                .totalQuantity(cart.getTotalQuantity())
                .totalAmount(cart.getTotalAmount())
                .build();
    }

    /**
     * 获取当前用户购物车
     */
    public CartResponse getCurrentCart(Long userId) {
        if (userId == null) {
            return CartResponse.builder()
                    .cartId(null)
                    .totalQuantity(0)
                    .totalAmount(BigDecimal.ZERO)
                    .items(Collections.emptyList())
                    .build();
        }

        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Cart cart = cartDao.findByUser(user);
        if (cart == null) {
            return CartResponse.builder()
                    .cartId(null)
                    .totalQuantity(0)
                    .totalAmount(BigDecimal.ZERO)
                    .items(Collections.emptyList())
                    .build();
        }

        List<CartItem> cartItems = cartItemDao.findByCart(cart);
        List<CartItemVO> itemVOs = cartItems.stream()
                .map(this::toCartItemVO)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .cartId(cart.getId())
                .totalQuantity(cart.getTotalQuantity())
                .totalAmount(cart.getTotalAmount())
                .items(itemVOs)
                .build();
    }

    /**
     * 修改购物车商品数量
     */
    public void updateCartItem(Long userId, Long itemId, CartItemUpdateRequest request) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        CartItem cartItem = cartItemDao.findById(itemId);
        if (cartItem == null) {
            throw new RuntimeException("购物车商品项不存在");
        }

        Cart cart = cartItem.getCart();
        if (!cart.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此购物车商品");
        }

        if (!"active".equals(cart.getCartStatus())) {
            throw new RuntimeException("购物车状态异常");
        }

        Integer quantity = request.getQuantity();
        if (quantity == null || quantity < 0) {
            throw new RuntimeException("数量不合法");
        }

        if (quantity > 0) {
            Product product = cartItem.getProduct();
            if (product != null) {
                if (product.getStatus() != 1) {
                    throw new RuntimeException("商品已下架");
                }
                if (quantity > product.getStock()) {
                    throw new RuntimeException("库存不足");
                }
            }
            cartItem.setQuantity(quantity);
            cartItem.setSubtotalAmount(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
            cartItemDao.save(cartItem);
        } else {
            cartItemDao.delete(cartItem);
        }

        updateCartSummary(cart);
    }

    private CartItemVO toCartItemVO(CartItem cartItem) {
        List<OptionVO> options = parseOptionSnapshot(cartItem.getOptionSnapshot());

        Product product = cartItem.getProduct();
        Long productId = product != null ? product.getId() : null;

        boolean invalid = product == null
                || product.getStatus() != 1
                || product.getStock() <= 0;

        return CartItemVO.builder()
                .itemId(cartItem.getId())
                .productId(productId)
                .productName(cartItem.getProductNameSnapshot())
                .productImage(cartItem.getProductImageSnapshot())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .subtotalAmount(cartItem.getSubtotalAmount())
                .options(options)
                .invalid(invalid)
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

    /**
     * 更新购物车汇总数据
     */
    private void updateCartSummary(Cart cart) {
        List<CartItem> items = cartItemDao.findByCart(cart);

        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : items) {
            totalQuantity += item.getQuantity();
            totalAmount = totalAmount.add(item.getSubtotalAmount());
        }

        cart.setTotalQuantity(totalQuantity);
        cart.setTotalAmount(totalAmount);
        cartDao.save(cart);
    }

    /**
     * 比较两个optionSnapshot是否相同
     */
    private boolean isSameOptionSnapshot(String snapshot1, String snapshot2) {
        if (snapshot1 == null && snapshot2 == null) {
            return true;
        }
        if (snapshot1 == null || snapshot2 == null) {
            return false;
        }
        List<OptionItem> list1 = parseOptionItems(snapshot1);
        List<OptionItem> list2 = parseOptionItems(snapshot2);
        if (list1 == null && list2 == null) {
            return true;
        }
        if (list1 == null || list2 == null) {
            return false;
        }
        return list1.equals(list2);
    }

    private List<OptionItem> parseOptionItems(String snapshot) {
        if (snapshot == null || snapshot.trim().isEmpty()) {
            return null;
        }
        try {
            List<OptionItem> list = objectMapper.readValue(
                    snapshot,
                    new TypeReference<List<OptionItem>>() {}
            );
            list.sort((o1, o2) -> Long.compare(o1.getGroupId(), o2.getGroupId()));
            return list;
        } catch (Exception e) {
            log.error("规格快照解析失败, snapshot={}", snapshot, e);
            return null;
        }
    }

    /**
     * 标准化规格快照JSON
     */
    private String normalizeOptionSnapshot(String optionSnapshot) {
        if (optionSnapshot == null || optionSnapshot.trim().isEmpty()) {
            return null;
        }

        try {
            List<OptionItem> optionList = objectMapper.readValue(
                    optionSnapshot,
                    new TypeReference<List<OptionItem>>() {}
            );
            optionList.sort((o1, o2) -> Long.compare(o1.getGroupId(), o2.getGroupId()));
            return objectMapper.writeValueAsString(optionList);
        } catch (Exception e) {
            return optionSnapshot;
        }
    }

    /**
     * 规格项内部类
     */
    @Data
    private static class OptionItem {
        private Long groupId;
        private Long optionId;
    }
}
