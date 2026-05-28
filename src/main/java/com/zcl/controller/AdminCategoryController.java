package com.zcl.controller;

import com.zcl.dto.CategoryCreateRequest;
import com.zcl.dto.Result;
import com.zcl.entity.ProductCategory;
import com.zcl.service.AdminCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminCategoryService categoryService;

    public AdminCategoryController(AdminCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Result<List<ProductCategory>>> getCategoryList() {
        return ResponseEntity.ok(Result.success("获取成功", categoryService.getCategoryList()));
    }

    @PostMapping
    public ResponseEntity<Result<ProductCategory>> createCategory(@RequestBody CategoryCreateRequest request) {
        if (request.getCategoryName() == null || request.getCategoryName().isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error(400, "分类名称不能为空"));
        }
        return ResponseEntity.ok(Result.success("创建成功", categoryService.createCategory(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<ProductCategory>> updateCategory(@PathVariable Long id,
                                                                    @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.ok(Result.success("更新成功", categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(Result.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Result.error(400, e.getMessage()));
        }
    }
}
