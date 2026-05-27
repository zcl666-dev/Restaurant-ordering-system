package com.zcl.controller;

import com.zcl.dto.Result;
import com.zcl.dto.WxLoginRequest;
import com.zcl.dto.WxLoginResponse;
import com.zcl.service.WxLoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wx")
public class WxLoginController {

    private static final Logger log = LoggerFactory.getLogger(WxLoginController.class);

    private final WxLoginService wxLoginService;

    public WxLoginController(WxLoginService wxLoginService) {
        this.wxLoginService = wxLoginService;
    }

    /**
     * 微信登录接口
     * 前端传递 code、nickName、avatarUrl，后端返回 JWT Token 和用户信息
     */
    @PostMapping("/login")
    public ResponseEntity<Result<WxLoginResponse>> login(@RequestBody WxLoginRequest request) {
        // 参数验证
        if (request.getCode() == null || request.getCode().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, "微信临时code不能为空"));
        }
        if (request.getNickName() == null || request.getNickName().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, "昵称不能为空"));
        }
        if (request.getAvatarUrl() == null || request.getAvatarUrl().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Result.error(400, "头像地址不能为空"));
        }

        try {
            // 调用服务层，获取登录响应（包含 Token）
            WxLoginResponse response = wxLoginService.login(request);
            
            // 在控制台输出提示信息
            log.info("Token成功发送");
            
            return ResponseEntity.ok(Result.success("登录成功", response));
        } catch (RuntimeException e) {
            log.error("微信登录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error(500, "登录失败: " + e.getMessage()));
        }
    }
}
