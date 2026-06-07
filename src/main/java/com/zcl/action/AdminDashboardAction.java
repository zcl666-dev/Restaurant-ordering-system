package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.AdminDashboardVO;
import com.zcl.dto.Result;
import com.zcl.dto.SalesStatsVO;
import com.zcl.dto.TopProductVO;
import com.zcl.service.AdminDashboardService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
@Scope("prototype")
public class AdminDashboardAction extends ActionSupport {

    @Autowired
    private AdminDashboardService adminDashboardService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private String period;
    private int limit = 10;

    private void writeJson(Result<?> result) {
        try {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String stats() {
        try {
            AdminDashboardVO stats = adminDashboardService.getDashboardStats();
            writeJson(Result.success("获取成功", stats));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取统计数据失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String sales() {
        try {
            List<SalesStatsVO> salesStats = adminDashboardService.getSalesStats(period);
            writeJson(Result.success("获取成功", salesStats));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取销售统计失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String topProducts() {
        try {
            List<TopProductVO> topProducts = adminDashboardService.getTopProducts(limit);
            writeJson(Result.success("获取成功", topProducts));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取热销商品失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String orderStatus() {
        try {
            Map<Integer, Long> distribution = adminDashboardService.getOrderStatusDistribution();
            writeJson(Result.success("获取成功", distribution));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取订单状态分布失败: " + e.getMessage()));
        }
        return NONE;
    }

    // Getters and Setters
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
}
