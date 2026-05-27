package com.zcl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcl.dto.WxLoginRequest;
import com.zcl.dto.WxLoginResponse;
import com.zcl.entity.User;
import com.zcl.repository.UserRepository;
import com.zcl.util.JwtUtil;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WxLoginService {

    private static final Logger log = LoggerFactory.getLogger(WxLoginService.class);

    private static final String WX_CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    @Value("${wx.mini.appid}")
    private String appid;

    @Value("${wx.mini.secret}")
    private String secret;

    private final UserRepository userRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    public WxLoginService(UserRepository userRepository, ObjectMapper objectMapper, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
        this.jwtUtil = jwtUtil;
    }

    /**
     * 微信登录，返回 JWT Token 和用户信息
     *
     * @param request 登录请求参数
     * @return 登录响应（包含 Token 和用户信息）
     */
    public WxLoginResponse login(WxLoginRequest request) {
        // 1. 用临时 code 去微信服务器换取唯一 openid
        String openId = code2OpenId(request.getCode());
        if (openId == null || openId.isEmpty()) {
            throw new RuntimeException("微信登录失败，无法获取openid");
        }

        // 2. 查询数据库：有没有这个 openid
        User user = userRepository.findByOpenId(openId)
                .map(existingUser -> {
                    // 用户存在，更新昵称和头像
                    existingUser.setNickName(request.getNickName());
                    existingUser.setAvatarUrl(request.getAvatarUrl());
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    // 3. 没有，新增一条用户数据
                    User newUser = new User();
                    newUser.setOpenId(openId);
                    newUser.setNickName(request.getNickName());
                    newUser.setAvatarUrl(request.getAvatarUrl());
                    return userRepository.save(newUser);
                });

        // 7. 后端生成 JWT Token（里面包含 openid、用户 ID）
        String token = jwtUtil.generateToken(user.getOpenId(), user.getId(), user.getNickName());

        // 8. 构建响应对象
        WxLoginResponse response = new WxLoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setOpenId(user.getOpenId());
        response.setNickName(user.getNickName());
        response.setAvatarUrl(user.getAvatarUrl());

        return response;
    }

    /**
     * 调用微信接口，用 code 换取 openid
     */
    private String code2OpenId(String code) {
        String url = String.format(WX_CODE2SESSION_URL, appid, secret, code);
        try {
            String responseBody = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            log.info("微信code2session响应: {}", responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                String errmsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                log.error("微信接口返回错误: errcode={}, errmsg={}", jsonNode.get("errcode").asInt(), errmsg);
                throw new RuntimeException("微信接口调用失败: " + errmsg);
            }

            return jsonNode.has("openid") ? jsonNode.get("openid").asText() : null;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信code2session接口异常", e);
            throw new RuntimeException("调用微信登录接口失败", e);
        }
    }
}
