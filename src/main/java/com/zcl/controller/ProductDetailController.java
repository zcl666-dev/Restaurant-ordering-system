package com.zcl.controller;

import com.zcl.dto.ProductDetailVO;
import com.zcl.dto.Result;
import com.zcl.service.ProductDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品详情控制器
 */
@RestController
@RequestMapping("/api/product")
public class ProductDetailController {

    @Autowired
    private ProductDetailService productDetailService;

    /**
     * 获取商品详情
     * 根据商品ID查询商品详细信息，包括规格组和规格值
     * 
     * 请求示例：
     * GET /api/product/1
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "获取成功",
     *   "data": {
     *     "id": 1,
     *     "productName": "招牌红烧肉",
     *     "productImage": "/static/p1.jpg",
     *     "description": "肥而不腻，入口即化",
     *     "price": 48.00,
     *     "stock": 100,
     *     "salesCount": 999,
     *     "status": 1,
     *     "productType": 0,
     *     "optionGroups": [
     *       {
     *         "groupId": 1,
     *         "groupName": "辣度",
     *         "options": [
     *           {
     *             "id": 1,
     *             "valueName": "不辣",
     *             "isDefault": true
     *           },
     *           {
     *             "id": 2,
     *             "valueName": "微辣",
     *             "isDefault": false
     *           }
     *         ]
     *       }
     *     ]
     *   }
     * }
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<ProductDetailVO>> getProductDetail(@PathVariable Long id) {
        try {
            ProductDetailVO detail = productDetailService.getProductDetail(id);
            
            if (detail == null) {
                return ResponseEntity.ok(Result.error(404, "商品不存在"));
            }
            
            return ResponseEntity.ok(Result.success("获取成功", detail));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Result.error(500, "获取商品详情失败: " + e.getMessage()));
        }
    }
}
