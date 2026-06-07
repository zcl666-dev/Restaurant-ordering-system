package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.CategoryWithProductsVO;
import com.zcl.dto.ProductDetailVO;
import com.zcl.dto.Result;
import com.zcl.service.ProductDisplayService;
import com.zcl.service.ProductDetailService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@Scope("prototype")
public class ProductAction extends ActionSupport {

    @Autowired
    private ProductDisplayService productDisplayService;

    @Autowired
    private ProductDetailService productDetailService;

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

    public String display() {
        try {
            List<CategoryWithProductsVO> categories = productDisplayService.getCategoriesWithProducts();
            writeJson(Result.success("获取成功", categories));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取商品列表失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String detail() {
        try {
            ProductDetailVO product = productDetailService.getProductDetail(id);
            writeJson(Result.success("获取成功", product));
        } catch (Exception e) {
            writeJson(Result.error(404, e.getMessage()));
        }
        return NONE;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
