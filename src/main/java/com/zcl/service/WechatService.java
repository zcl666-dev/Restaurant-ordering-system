package com.zcl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序服务封装
 * 提供：AccessToken缓存、小程序码生成
 */
@Service
public class WechatService {

    private static final Logger log = LoggerFactory.getLogger(WechatService.class);

    private static final String WX_ACCESS_TOKEN_URL =
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String WX_GET_WXACODE_UNLIMITED_URL =
            "https://api.weixin.qq.com/wxa/getwxacodeunlimited?access_token=%s";

    @Autowired
    private WxConfigService wxConfigService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String cachedAccessToken;
    private long accessTokenExpireTime;

    /**
     * 获取 access_token（带缓存，过期前300秒自动刷新）
     */
    public synchronized String getAccessToken() {
        if (cachedAccessToken != null && System.currentTimeMillis() < accessTokenExpireTime) {
            return cachedAccessToken;
        }

        String url = String.format(WX_ACCESS_TOKEN_URL,
                wxConfigService.getAppId(), wxConfigService.getSecret());

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                String errMsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                throw new RuntimeException("获取微信access_token失败: " + errMsg);
            }

            cachedAccessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            accessTokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;

            log.info("获取access_token成功，有效期{}秒", expiresIn);
            return cachedAccessToken;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("获取微信access_token失败: " + e.getMessage());
        }
    }

    /**
     * 生成微信小程序码（失败直接抛异常，返回具体原因）
     * @param scene  场景值，如 "tableNo=A01"
     * @param page   小程序页面路径，如 "pages/login/login"
     * @param width  宽度
     * @return 图片字节数组（PNG格式）
     */
    public byte[] generateQrCode(String scene, String page, int width) {
        String accessToken = getAccessToken();
        String url = String.format(WX_GET_WXACODE_UNLIMITED_URL, accessToken);

        System.out.println("[DEBUG] getwxacodeunlimited URL: " + url);
        System.out.println("[DEBUG] scene=" + scene + ", page=" + page + ", width=" + width);

        Map<String, Object> params = new HashMap<>();
        params.put("scene", scene);
        params.put("page", page);
        params.put("check_path", false);
        params.put("width", width);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            throw new RuntimeException("JSON序列化失败: " + e.getMessage());
        }
        System.out.println("[DEBUG] JSON body: " + jsonBody);

        byte[] imageData;
        try {
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("[DEBUG] HTTP response code: " + responseCode);

            if (responseCode == 200) {
                try (java.io.InputStream is = conn.getInputStream()) {
                    imageData = is.readAllBytes();
                }
                System.out.println("[DEBUG] response length: " + (imageData == null ? "null" : imageData.length));
                if (imageData != null && imageData.length > 0) {
                    System.out.println("[DEBUG] first byte: " + imageData[0] + " (0x" + String.format("%02X", imageData[0]) + ")");
                }
            } else {
                try (java.io.InputStream es = conn.getErrorStream()) {
                    String errBody = new String(es.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    System.out.println("[DEBUG] error body: " + errBody);
                    throw new RuntimeException("HTTP " + responseCode + ": " + errBody);
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("调用微信接口失败: " + e.getMessage());
        }

        if (imageData == null || imageData.length == 0) {
            throw new RuntimeException("微信返回数据为空");
        }

        // 微信API错误时返回JSON（以'{'开头）
        if (imageData[0] == '{') {
            String errJson = new String(imageData, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("[DEBUG] 微信返回JSON: " + errJson);
            try {
                JsonNode errNode = objectMapper.readTree(errJson);
                int errCode = errNode.has("errcode") ? errNode.get("errcode").asInt() : -1;
                String errMsg = errNode.has("errmsg") ? errNode.get("errmsg").asText() : errJson;

                // access_token过期，清除缓存后重试一次
                if (errCode == 40001 || errCode == 42001) {
                    log.warn("access_token过期，清除缓存重试");
                    cachedAccessToken = null;
                    accessTokenExpireTime = 0;
                    return generateQrCode(scene, page, width);
                }

                throw new RuntimeException("微信生成小程序码失败: errcode=" + errCode + ", errmsg=" + errMsg);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("微信返回异常: " + errJson);
            }
        }

        log.info("微信小程序码生成成功");
        return imageData;
    }

    /**
     * 将图片字节数组转为 base64 data URL
     */
    public String toDataUrl(byte[] imageData) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
    }

    /**
     * 从 base64 data URL 解码为字节数组
     */
    public byte[] fromDataUrl(String dataUrl) {
        String base64 = dataUrl.contains(",") ? dataUrl.split(",")[1] : dataUrl;
        return Base64.getDecoder().decode(base64);
    }
}
