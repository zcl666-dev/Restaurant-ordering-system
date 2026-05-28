package com.zcl.controller;

import com.zcl.dto.AdminProductVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.ProductCreateRequest;
import com.zcl.dto.Result;
import com.zcl.entity.Product;
import com.zcl.service.AdminProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductService productService;

    public AdminProductController(AdminProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Result<PageResult<AdminProductVO>>> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(Result.success("获取成功",
                productService.getProductList(page, size, status, categoryId, keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<AdminProductVO>> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success("获取成功", productService.getProductDetail(id)));
    }

    @PostMapping
    public ResponseEntity<Result<Product>> createProduct(@RequestBody ProductCreateRequest request) {
        if (request.getProductName() == null || request.getProductName().isEmpty()) {
            return ResponseEntity.badRequest().body(Result.error(400, "商品名称不能为空"));
        }
        if (request.getCategoryId() == null) {
            return ResponseEntity.badRequest().body(Result.error(400, "商品分类不能为空"));
        }
        return ResponseEntity.ok(Result.success("创建成功", productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Product>> updateProduct(@PathVariable Long id,
                                                          @RequestBody ProductCreateRequest request) {
        return ResponseEntity.ok(Result.success("更新成功", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Result<Void>> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.toggleProductStatus(id, status);
        return ResponseEntity.ok(Result.success("状态更新成功", null));
    }
}
