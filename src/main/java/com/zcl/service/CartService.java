package com.zcl.service;

import com.zcl.dto.CartAddRequest;
import com.zcl.dto.CartAddResponse;
import com.zcl.dto.CartItemUpdateRequest;
import com.zcl.dto.CartItemVO;
import com.zcl.dto.CartResponse;
import com.zcl.dto.OptionVO;
import com.zcl.entity.Cart;
import com.zcl.entity.CartItem;
import com.zcl.entity.OptionGroup;
import com.zcl.entity.OptionValue;
import com.zcl.entity.Product;
import com.zcl.entity.User;
import com.zcl.repository.CartItemRepository;
import com.zcl.repository.CartRepository;
import com.zcl.repository.OptionGroupRepository;
import com.zcl.repository.OptionValueRepository;
import com.zcl.repository.ProductRepository;
import com.zcl.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 购物车服务类
 */
@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

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

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 加入购物车
     *
     * @param request 请求参数
     * @return 购物车信息
     */
    @Transactional(rollbackFor = Exception.class)
    public CartAddResponse addToCart(CartAddRequest request) {
        // 1. 从 token 获取 userId
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 2. 查询或创建购物车
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Cart cart = cartRepository.findByUserAndCartStatus(user, "active")
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCartStatus("active");
                    newCart.setTotalQuantity(0);
                    newCart.setTotalAmount(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });

        // 3. 查询商品信息
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 检查商品是否上架
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架");
        }

        // 处理规格快照（如果为空则设为null，否则进行排序）
        String optionSnapshot = normalizeOptionSnapshot(request.getOptionSnapshot());
        log.info("加入购物车: productId={}, optionSnapshot={}", request.getProductId(), optionSnapshot);

        // 4. 判断商品是否已存在（先按cart和product查询，再在Java层比较optionSnapshot）
        List<CartItem> existingItems = cartItemRepository.findByCartAndProduct(cart, product);
        log.info("购物车已有同商品项数: {}", existingItems.size());
        for (CartItem ci : existingItems) {
            log.info("已有项 itemId={}, optionSnapshot={}, quantity={}", ci.getId(), ci.getOptionSnapshot(), ci.getQuantity());
        }
        Optional<CartItem> existingItemOpt = existingItems.stream()
                .filter(item -> isSameOptionSnapshot(item.getOptionSnapshot(), optionSnapshot))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            log.info("找到相同规格项, 执行数量+1");
            // 5. 已存在商品，数量 +1
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + 1;
            
            // 库存校验
            if (newQuantity > product.getStock()) {
                throw new RuntimeException("库存不足");
            }
            
            existingItem.setQuantity(newQuantity);
            existingItem.setSubtotalAmount(
                    existingItem.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity))
            );
            cartItemRepository.save(existingItem);
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
            cartItemRepository.save(newItem);
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
     *
     * @return 购物车响应数据
     */
    public CartResponse getCurrentCart() {
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            return CartResponse.builder()
                    .cartId(null)
                    .totalQuantity(0)
                    .totalAmount(BigDecimal.ZERO)
                    .items(Collections.emptyList())
                    .build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return cartRepository.findByUserAndCartStatus(user, "active")
                .map(cart -> {
                    List<CartItem> cartItems = cartItemRepository.findByCart(cart);
                    List<CartItemVO> itemVOs = cartItems.stream()
                            .map(this::toCartItemVO)
                            .collect(Collectors.toList());

                    return CartResponse.builder()
                            .cartId(cart.getId())
                            .totalQuantity(cart.getTotalQuantity())
                            .totalAmount(cart.getTotalAmount())
                            .items(itemVOs)
                            .build();
                })
                .orElseGet(() -> CartResponse.builder()
                        .cartId(null)
                        .totalQuantity(0)
                        .totalAmount(BigDecimal.ZERO)
                        .items(Collections.emptyList())
                        .build());
    }

    /**
     * 修改购物车商品数量
     *
     * @param itemId   购物车商品项ID
     * @param request  请求参数（quantity）
     * @return 更新后的购物车数据
     */
    @Transactional(rollbackFor = Exception.class)
    public CartResponse updateCartItem(Long itemId, CartItemUpdateRequest request) {
        Long userId = userInfoService.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("购物车商品项不存在"));

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
            cartItemRepository.save(cartItem);
        } else {
            cartItemRepository.delete(cartItem);
        }

        updateCartSummary(cart);

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
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

    private CartItemVO toCartItemVO(CartItem cartItem) {
        List<OptionVO> options = parseOptionSnapshot(cartItem.getOptionSnapshot());

        Product product = null;
        Long productId = null;
        try {
            product = cartItem.getProduct();
            productId = product.getId();
        } catch (Exception e) {
            log.error("获取购物车商品信息失败, cartItemId={}", cartItem.getId(), e);
        }

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

    /**
     * 更新购物车汇总数据
     */
    private void updateCartSummary(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCart(cart);

        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : items) {
            totalQuantity += item.getQuantity();
            totalAmount = totalAmount.add(item.getSubtotalAmount());
        }

        cart.setTotalQuantity(totalQuantity);
        cart.setTotalAmount(totalAmount);
        cartRepository.save(cart);
    }

    /**
     * 比较两个optionSnapshot是否相同
     * 解析为对象列表后直接比较，彻底消除JSON格式差异
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
     * 标准化规格快照JSON（按groupId升序排序）
     *
     * @param optionSnapshot 原始规格JSON字符串
     * @return 排序后的JSON字符串，如果为空则返回null
     */
    private String normalizeOptionSnapshot(String optionSnapshot) {
        if (optionSnapshot == null || optionSnapshot.trim().isEmpty()) {
            return null;
        }

        try {
            // 解析JSON数组
            List<OptionItem> optionList = objectMapper.readValue(
                    optionSnapshot,
                    new TypeReference<List<OptionItem>>() {}
            );

            // 按groupId升序排序
            optionList.sort((o1, o2) -> Long.compare(o1.getGroupId(), o2.getGroupId()));

            // 转回JSON字符串
            return objectMapper.writeValueAsString(optionList);
        } catch (Exception e) {
            // 如果解析失败，返回原始字符串
            return optionSnapshot;
        }
    }

    /**
     * 规格项内部类（用于JSON解析和排序）
     */
    @Data
    private static class OptionItem {
        private Long groupId;
        private Long optionId;
    }
}
