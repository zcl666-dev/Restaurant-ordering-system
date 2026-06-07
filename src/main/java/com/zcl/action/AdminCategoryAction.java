package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.CategoryCreateRequest;
import com.zcl.dto.Result;
import com.zcl.entity.ProductCategory;
import com.zcl.service.AdminCategoryService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@Scope("prototype")
public class AdminCategoryAction extends ActionSupport {

    @Autowired
    private AdminCategoryService adminCategoryService;

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

    public String list() {
        try {
            List<ProductCategory> categories = adminCategoryService.getCategoryList();
            writeJson(Result.success("获取成功", categories));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取分类列表失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String create() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            CategoryCreateRequest categoryRequest = objectMapper.readValue(request.getInputStream(), CategoryCreateRequest.class);
            adminCategoryService.createCategory(categoryRequest);
            writeJson(Result.success("创建成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "创建分类失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String update() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            CategoryCreateRequest categoryRequest = objectMapper.readValue(request.getInputStream(), CategoryCreateRequest.class);
            adminCategoryService.updateCategory(id, categoryRequest);
            writeJson(Result.success("更新成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "更新分类失败: " + e.getMessage()));
        }
        return NONE;
    }

    public String delete() {
        try {
            adminCategoryService.deleteCategory(id);
            writeJson(Result.success("删除成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "删除分类失败: " + e.getMessage()));
        }
        return NONE;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
