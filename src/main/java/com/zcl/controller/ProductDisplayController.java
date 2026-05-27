package com.zcl.controller;

import com.zcl.dto.CategoryWithProductsVO;
import com.zcl.dto.Result;
import com.zcl.service.ProductDisplayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品展示控制器
 */
@RestController
@RequestMapping("/api/product")
public class ProductDisplayController {

    @Autowired
    private ProductDisplayService productDisplayService;

    /**
     * 获取商品展示数据
     * 返回所有启用的商品分类，每个分类下包含该分类的上架商品（嵌套结构）
     * 
     * 请求示例：
     * GET /api/product/display
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "获取成功",
     *   "data": [
     *     {
     *       "id": 1,
     *       "categoryName": "热销",
     *       "icon": "/static/icons/hot.png",
     *       "products": [
     *         {
     *           "id": 1,
     *           "productName": "红烧肉",
     *           "productImage": "/static/p1.jpg",
     *           "price": 48.00
     *         }
     *       ]
     *     }
     *   ]
     * }
     */
    @GetMapping("/display")
    public ResponseEntity<Result<List<CategoryWithProductsVO>>> getProductDisplay() {
        try {
            List<CategoryWithProductsVO> response = productDisplayService.getProductDisplayData();
            return ResponseEntity.ok(Result.success("获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Result.error(500, "获取商品数据失败: " + e.getMessage()));
        }
    }
}
