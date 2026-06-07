package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.CartAddRequest;
import com.zcl.dto.CartAddResponse;
import com.zcl.dto.CartItemUpdateRequest;
import com.zcl.dto.CartResponse;
import com.zcl.dto.Result;
import com.zcl.service.CartService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Scope("prototype")
public class CartAction extends ActionSupport {

    @Autowired
    private CartService cartService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Long itemId;

    private void writeJson(Result<?> result) {
        try {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String add() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            CartAddRequest addRequest = objectMapper.readValue(request.getInputStream(), CartAddRequest.class);
            Long userId = (Long) request.getAttribute("userId");
            CartAddResponse response = cartService.addToCart(userId, addRequest);
            writeJson(Result.success("添加成功", response));
        } catch (Exception e) {
            writeJson(Result.error(500, "添加购物车失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String current() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            CartResponse cart = cartService.getCurrentCart(userId);
            writeJson(Result.success("获取成功", cart));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取购物车失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String updateItem() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            CartItemUpdateRequest updateRequest = objectMapper.readValue(request.getInputStream(), CartItemUpdateRequest.class);
            Long userId = (Long) request.getAttribute("userId");
            cartService.updateCartItem(userId, itemId, updateRequest);
            writeJson(Result.success("更新成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "更新购物车失败: " + e.getMessage()));
        }
        return NONE;
    }

    // Getters and Setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
}
