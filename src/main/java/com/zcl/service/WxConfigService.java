package com.zcl.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 微信小程序配置集中管理
 */
@Service
public class WxConfigService {

    @Value("${wx.mini.appid}")
    private String appId;

    @Value("${wx.mini.secret}")
    private String secret;

    public String getAppId() {
        return appId;
    }

    public String getSecret() {
        return secret;
    }
}
