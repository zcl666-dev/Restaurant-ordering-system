package com.zcl.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.OrderCreateResponse;
import com.zcl.dto.OrderDetailVO;
import com.zcl.dto.OrderListVO;
import com.zcl.dto.Result;
import com.zcl.service.OrderService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@Scope("prototype")
public class OrderAction extends ActionSupport {

    @Autowired
    private OrderService orderService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Long id;

    private void writeJson(Result<?> result) {
        try {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String create() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            JsonNode jsonNode = objectMapper.readTree(request.getInputStream());
            Integer diningType = null;
            String tableNumber = null;
            String remark = null;
            if (jsonNode.has("diningType")) {
                diningType = jsonNode.get("diningType").asInt();
            }
            if (jsonNode.has("tableNumber")) {
                tableNumber = jsonNode.get("tableNumber").asText();
            }
            if (jsonNode.has("remark")) {
                remark = jsonNode.get("remark").asText();
            }
            Long userId = (Long) request.getAttribute("userId");
            OrderCreateResponse response = orderService.createOrder(userId, diningType, tableNumber, remark);
            writeJson(Result.success("下单成功", response));
        } catch (Exception e) {
            writeJson(Result.error(500, "创建订单失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String detail() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            OrderDetailVO order = orderService.getOrderDetail(userId, id);
            writeJson(Result.success("获取成功", order));
        } catch (Exception e) {
            writeJson(Result.error(404, e.getMessage()));
        }
        return NONE;
    }

    public String list() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            List<OrderListVO> orders = orderService.getOrderList(userId);
            writeJson(Result.success("获取成功", orders));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取订单列表失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String cancel() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            orderService.cancelOrder(userId, id);
            writeJson(Result.success("取消成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "取消订单失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String pay() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            orderService.payOrder(userId, id);
            writeJson(Result.success("支付成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "支付订单失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String updateDiningType() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            JsonNode jsonNode = objectMapper.readTree(request.getInputStream());
            Integer diningType = jsonNode.get("diningType").asInt();
            orderService.updateDiningType(userId, id, diningType);
            writeJson(Result.success("更新成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "更新就餐方式失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String complete() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            orderService.completeOrder(userId, id);
            writeJson(Result.success("完成成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "完成订单失败: " + e.getMessage()));
        }
        return NONE;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
