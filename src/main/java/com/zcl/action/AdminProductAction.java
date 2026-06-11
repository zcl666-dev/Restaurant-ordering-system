package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.AdminProductVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.ProductCreateRequest;
import com.zcl.dto.Result;
import com.zcl.service.AdminProductService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Scope("prototype")
public class AdminProductAction extends ActionSupport {

    @Autowired
    private AdminProductService adminProductService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Long id;
    private int page = 0;
    private int size = 10;
    private Integer status;
    private Long categoryId;
    private String keyword;

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
            PageResult<AdminProductVO> pageResult = adminProductService.getProductList(page, size, status, categoryId, keyword);
            writeJson(Result.success("获取成功", pageResult));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取商品列表失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String detail() {
        try {
            AdminProductVO product = adminProductService.getProductDetail(id);
            writeJson(Result.success("获取成功", product));
        } catch (Exception e) {
            writeJson(Result.error(404, e.getMessage()));
        }
        return NONE;
    }

    public String create() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            ProductCreateRequest productRequest = objectMapper.readValue(request.getInputStream(), ProductCreateRequest.class);
            adminProductService.createProduct(productRequest);
            writeJson(Result.success("创建成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "创建商品失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String update() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            ProductCreateRequest productRequest = objectMapper.readValue(request.getInputStream(), ProductCreateRequest.class);
            Long productId = productRequest.getId() != null ? productRequest.getId() : id;
            adminProductService.updateProduct(productId, productRequest);
            writeJson(Result.success("更新成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "更新商品失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String delete() {
        try {
            adminProductService.deleteProduct(id);
            writeJson(Result.success("删除成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "删除商品失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String toggleStatus() {
        try {
            adminProductService.toggleProductStatus(id, status);
            writeJson(Result.success("状态更新成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "更新状态失败: " + e.getMessage()));
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
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
