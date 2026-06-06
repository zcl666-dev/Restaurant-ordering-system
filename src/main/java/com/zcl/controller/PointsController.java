package com.zcl.controller;

import com.zcl.dto.PointsDetailVO;
import com.zcl.dto.Result;
import com.zcl.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @GetMapping("/detail")
    public ResponseEntity<Result<PointsDetailVO>> getPointsDetail() {
        try {
            PointsDetailVO response = pointsService.getPointsDetail();
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
