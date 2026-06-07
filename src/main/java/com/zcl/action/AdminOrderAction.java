package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.AdminOrderDetailVO;
import com.zcl.dto.AdminOrderVO;
import com.zcl.dto.AdminOrderUpdateRequest;
import com.zcl.dto.PageResult;
import com.zcl.dto.Result;
import com.zcl.service.AdminOrderService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
}
