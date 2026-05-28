package com.zcl.controller;

import com.zcl.dto.AdminOrderDetailVO;
import com.zcl.dto.AdminOrderUpdateRequest;
import com.zcl.dto.AdminOrderVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.Result;
import com.zcl.service.AdminOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService orderService;

    public AdminOrderController(AdminOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Result<PageResult<AdminOrderVO>>> getOrderList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(Result.success("获取成功",
                orderService.getOrderList(page, size, status, keyword, startDate, endDate)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<AdminOrderDetailVO>> getOrderDetail(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success("获取成功", orderService.getOrderDetail(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Result<Void>> updateOrderStatus(@PathVariable Long id,
                                                            @RequestBody AdminOrderUpdateRequest request) {
        if (request.getOrderStatus() == null) {
            return ResponseEntity.badRequest().body(Result.error(400, "订单状态不能为空"));
        }
        orderService.updateOrderStatus(id, request.getOrderStatus());
        return ResponseEntity.ok(Result.success("状态更新成功", null));
    }
}
