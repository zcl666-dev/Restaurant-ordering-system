package com.zcl.controller;

import com.zcl.dto.Result;
import com.zcl.dto.SubscribeSaveRequest;
import com.zcl.service.SubscribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscribe")
public class SubscribeController {

    @Autowired
    private SubscribeService subscribeService;

    @PostMapping("/save")
    public ResponseEntity<Result<Void>> save(@RequestBody SubscribeSaveRequest request) {
        if (request.getTemplateId() == null) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, "templateId不能为空"));
        }
        if (request.getStatus() == null) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, "status不能为空"));
        }

        try {
            subscribeService.save(request);
            return ResponseEntity.ok(Result.success("保存成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Result.error(500, "系统错误：" + e.getMessage()));
        }
    }
}
