package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.AdminOrderDetailVO;
import com.zcl.dto.AdminOrderVO;
import com.zcl.dto.AdminOrderUpdateRequest;
import com.zcl.dto.CategoryWithProductsVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.ProductDetailVO;
import com.zcl.dto.Result;
import com.zcl.dto.StaffOrderRequest;
import com.zcl.service.AdminOrderService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
@Scope("prototype")
public class AdminOrderAction extends ActionSupport {

    @Autowired
    private AdminOrderService adminOrderService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Long id;
    private int page = 0;
    private int size = 10;
    private Integer status;
    private String keyword;
    private String startDate;
    private String endDate;
    private Long lastOrderId;

    private void writeJson(Result<?> result) {
        try {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String list() {
        try {
            PageResult<AdminOrderVO> pageResult = adminOrderService.getOrderList(page, size, status, keyword, startDate, endDate);
            writeJson(Result.success("获取成功", pageResult));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取订单列表失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String detail() {
        try {
            AdminOrderDetailVO order = adminOrderService.getOrderDetail(id);
            writeJson(Result.success("获取成功", order));
        } catch (Exception e) {
            writeJson(Result.error(404, e.getMessage()));
        }
        return NONE;
    }

    public String updateStatus() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            AdminOrderUpdateRequest updateRequest = objectMapper.readValue(request.getInputStream(), AdminOrderUpdateRequest.class);
            adminOrderService.updateOrderStatus(id, updateRequest.getOrderStatus());
            writeJson(Result.success("状态更新成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "更新订单状态失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 餐厅开始制作
     */
    public String startProduction() {
        try {
            adminOrderService.startProduction(id);
            writeJson(Result.success("已开始制作", null));
        } catch (Exception e) {
            writeJson(Result.error(500, e.getMessage()));
        }
        return NONE;
    }

    /**
     * 餐厅拒绝订单
     */
    public String rejectOrder() {
        try {
            adminOrderService.rejectOrder(id);
            writeJson(Result.success("已拒绝订单并退款", null));
        } catch (Exception e) {
            writeJson(Result.error(500, e.getMessage()));
        }
        return NONE;
    }

    /**
     * 餐厅完成制作
     */
    public String completeProduction() {
        try {
            adminOrderService.completeProduction(id);
            writeJson(Result.success("订单已完成", null));
        } catch (Exception e) {
            writeJson(Result.error(500, e.getMessage()));
        }
        return NONE;
    }

    public String checkNew() {
        try {
            if (lastOrderId == null) {
                // 未传 lastOrderId 时，返回当前已支付订单最大 id（前端用于初始化）
                Long maxId = adminOrderService.getMaxPaidOrderId();
                writeJson(Result.success("获取成功", Map.of("lastOrderId", maxId, "newOrders", List.of())));
            } else {
                List<AdminOrderVO> newOrders = adminOrderService.getNewPaidOrders(lastOrderId);
                Long newMaxId = newOrders.isEmpty() ? lastOrderId :
                        newOrders.stream().mapToLong(AdminOrderVO::getId).max().orElse(lastOrderId);
                writeJson(Result.success("获取成功", Map.of("lastOrderId", newMaxId, "newOrders", newOrders)));
            }
        } catch (Exception e) {
            writeJson(Result.error(500, "检查新订单失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 获取未处理订单数量（状态=1 待制作）
     */
    public String unprocessedCount() {
        try {
            long count = adminOrderService.getUnprocessedCount();
            writeJson(Result.success("获取成功", Map.of("count", count)));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取未处理订单数失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 代客点餐 - 获取商品详情（含规格信息）
     */
    public String staffProductDetail() {
        try {
            ProductDetailVO detail = adminOrderService.getStaffProductDetail(id);
            writeJson(Result.success("获取成功", detail));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取商品详情失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 代客点餐 - 获取商品列表
     */
    public String staffProducts() {
        try {
            List<CategoryWithProductsVO> products = adminOrderService.getProductsForStaffOrder();
            writeJson(Result.success("获取成功", products));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取商品列表失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 代客点餐 - 创建订单
     */
    public String staffCreateOrder() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            StaffOrderRequest orderRequest = objectMapper.readValue(request.getInputStream(), StaffOrderRequest.class);
            Long orderId = adminOrderService.staffCreateOrder(orderRequest);
            writeJson(Result.success("下单成功", Map.of("orderId", orderId)));
        } catch (Exception e) {
            writeJson(Result.error(500, "下单失败: " + e.getMessage()));
        }
        return NONE;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Long getLastOrderId() { return lastOrderId; }
    public void setLastOrderId(Long lastOrderId) { this.lastOrderId = lastOrderId; }
}
