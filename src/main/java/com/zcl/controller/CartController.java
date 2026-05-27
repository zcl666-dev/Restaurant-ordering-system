package com.zcl.controller;

import com.zcl.dto.CartAddRequest;
import com.zcl.dto.CartAddResponse;
import com.zcl.dto.CartItemUpdateRequest;
import com.zcl.dto.CartResponse;
import com.zcl.dto.Result;
import com.zcl.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 加入购物车
     * 
     * 请求示例：
     * POST /api/cart/add
     * Header: Authorization: Bearer <your_jwt_token>
     * Body: {
     *   "productId": 1,
     *   "optionSnapshot": "{\"size\":\"L\",\"color\":\"red\"}",
     *   "quantity": 1
     * }
     */
    @PostMapping("/add")
    public ResponseEntity<Result<CartAddResponse>> addToCart(@RequestBody CartAddRequest request) {
        try {
            // 参数校验
            if (request.getProductId() == null) {
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "商品ID不能为空"));
            }

            CartAddResponse response = cartService.addToCart(request);
            
            return ResponseEntity.ok(Result.success("加入购物车成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Result.error(500, "系统错误：" + e.getMessage()));
        }
    }

    /**
     * 获取当前购物车
     *
     * 请求示例：
     * GET /api/cart/current
     * Header: Authorization: Bearer <your_jwt_token>
     *
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "获取成功",
     *   "data": {
     *     "cartId": 1,
     *     "totalQuantity": 3,
     *     "totalAmount": 88.00,
     *     "items": [...]
     *   }
     * }
     */
    @GetMapping("/current")
    public ResponseEntity<Result<CartResponse>> getCurrentCart() {
        try {
            CartResponse response = cartService.getCurrentCart();
            return ResponseEntity.ok(Result.success("获取成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Result.error(500, "系统错误：" + e.getMessage()));
        }
    }

    /**
     * 修改购物车商品数量
     *
     * 请求示例：
     * PUT /api/cart/item/{itemId}
     * Header: Authorization: Bearer <your_jwt_token>
     * Body: {
     *   "quantity": 3
     * }
     */
    @PutMapping("/item/{itemId}")
    public ResponseEntity<Result<CartResponse>> updateCartItem(
            @PathVariable Long itemId,
            @RequestBody CartItemUpdateRequest request) {
        try {
            if (request.getQuantity() == null || request.getQuantity() < 0) {
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "数量不合法"));
            }

            CartResponse response = cartService.updateCartItem(itemId, request);
            return ResponseEntity.ok(Result.success("修改成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Result.error(500, "系统错误：" + e.getMessage()));
        }
    }
}
