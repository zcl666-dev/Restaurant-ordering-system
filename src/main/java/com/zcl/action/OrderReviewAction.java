package com.zcl.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.Result;
import com.zcl.service.OrderReviewService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@Scope("prototype")
public class OrderReviewAction extends ActionSupport {

    @Autowired
    private OrderReviewService orderReviewService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Long id;
    private Integer rating;

    private void writeJson(Result<?> result) {
        try {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户提交评价
     * POST /api_review_create.action
     */
    public String create() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            Long userId = (Long) request.getAttribute("userId");

            JsonNode jsonNode = objectMapper.readTree(request.getInputStream());
            Long orderId = jsonNode.has("orderId") ? jsonNode.get("orderId").asLong() : null;
            Integer rating = jsonNode.has("rating") ? jsonNode.get("rating").asInt() : null;
            String content = jsonNode.has("content") ? jsonNode.get("content").asText() : null;

            orderReviewService.createReview(userId, orderId, rating, content);
            writeJson(Result.success("评价成功", null));
        } catch (RuntimeException e) {
            writeJson(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(Result.error(500, "评价失败"));
        }
        return NONE;
    }

    /**
     * 检查订单是否已评价
     * GET /api_review_check.action?id=xxx
     */
    public String check() {
        try {
            Map<String, Object> review = orderReviewService.getReviewByOrderId(id);
            if (review != null) {
                writeJson(Result.success("已评价", review));
            } else {
                writeJson(Result.success("未评价", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(Result.error(500, "查询失败"));
        }
        return NONE;
    }

    /**
     * 管理后台 - 分页查询评价列表
     * GET /api_admin_review_list.action?page=0&size=10&rating=5
     */
    public String list() {
        try {
            int page = 0;
            int size = 10;

            HttpServletRequest request = ServletActionContext.getRequest();
            String pageStr = request.getParameter("page");
            String sizeStr = request.getParameter("size");
            String ratingStr = request.getParameter("rating");

            if (pageStr != null) page = Integer.parseInt(pageStr);
            if (sizeStr != null) size = Integer.parseInt(sizeStr);
            Integer filterRating = ratingStr != null && !ratingStr.isEmpty() ? Integer.parseInt(ratingStr) : null;

            Map<String, Object> data = orderReviewService.getReviewList(filterRating, page, size);
            writeJson(Result.success("查询成功", data));
        } catch (Exception e) {
            e.printStackTrace();
            writeJson(Result.error(500, "查询失败"));
        }
        return NONE;
    }

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}
