package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.PointsDetailVO;
import com.zcl.dto.Result;
import com.zcl.entity.UserExchangeVoucher;

import java.util.Map;
import com.zcl.service.PointsService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Scope("prototype")
public class PointsAction extends ActionSupport {

    @Autowired
    private PointsService pointsService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private void writeJson(Result<?> result) {
        try {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String detail() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            PointsDetailVO detail = pointsService.getPointsDetail(userId);
            writeJson(Result.success("获取成功", detail));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取积分详情失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 用户兑换券列表
     */
    public String voucherList() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> data = pointsService.getVoucherList(userId);
            writeJson(Result.success("获取成功", data));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取兑换券列表失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 积分商城列表
     */
    public String mallList() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> data = pointsService.getMallList(userId);
            writeJson(Result.success("获取成功", data));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取积分商城失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 兑换积分商品
     */
    @SuppressWarnings("unchecked")
    public String exchange() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> body = objectMapper.readValue(request.getInputStream(), Map.class);
            Long mallId = Long.valueOf(body.get("pointsMallId").toString());
            UserExchangeVoucher voucher = pointsService.exchange(userId, mallId);
            writeJson(Result.success("兑换成功", voucher.getVoucherCode()));
        } catch (Exception e) {
            writeJson(Result.error(500, e.getMessage()));
        }
        return NONE;
    }
}
