package com.zcl.controller;

import com.zcl.dto.OrderCreateResponse;
import com.zcl.dto.OrderDetailVO;
import com.zcl.dto.OrderListVO;
import com.zcl.dto.Result;
import com.zcl.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Result<OrderCreateResponse>> createOrder() {
        try {
            OrderCreateResponse response = orderService.createOrder();
            return ResponseEntity.ok(Result.success("下单成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Result.error(500, "系统错误：" + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<OrderDetailVO>> getOrderDetail(@PathVariable Long id) {
        try {
            OrderDetailVO response = orderService.getOrderDetail(id);
            return ResponseEntity.ok(Result.success("获取成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Result.error(500, "系统错误：" + e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Result<Void>> cancelOrder(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok(Result.success("订单已取消", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Result.error(500, "系统错误：" + e.getMessage()));
        }
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<Result<Void>> payOrder(@PathVariable Long id) {
        try {
            orderService.payOrder(id);
            return ResponseEntity.ok(Result.success("支付成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Result.error(500, "系统错误：" + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Result<List<OrderListVO>>> getOrderList() {
        try {
            List<OrderListVO> response = orderService.getOrderList();
            return ResponseEntity.ok(Result.success("获取成功", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Result.error(500, "系统错误：" + e.getMessage()));
        }
    }
}