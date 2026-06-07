package com.zcl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcl.dao.OrderItemDao;
import com.zcl.dao.SubscribeTemplateDao;
import com.zcl.entity.OrderItem;
import com.zcl.entity.Orders;
import com.zcl.entity.SubscribeTemplate;
import com.zcl.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class WxSubscribeService {

    private static final Logger log = LoggerFactory.getLogger(WxSubscribeService.class);

    private static final String WX_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String WX_SUBSCRIBE_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s";

    // 模板ID
    private static final String TEMPLATE_ORDER_FINISH = "owRJNezMKIuvTypD-IS_CEcgYbe3rfPz2WdWQyR889c";
    private static final String TEMPLATE_ORDER_CANCEL = "J2kTKOFZHXW8TuRUYR-QfUPotm0uwS6ft0ikephjEjE";
    private static final String TEMPLATE_MEAL_REMIND = "iSuL7Y8g3WyG-4VM0tbFrEwvqB95LDqp71k4vx1OTvQ";

    @Value("${wx.mini.appid}")
    private String appid;

    @Value("${wx.mini.secret}")
    private String secret;

    @Autowired
    private SubscribeTemplateDao subscribeTemplateDao;

    @Autowired
    private OrderItemDao orderItemDao;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate = new RestTemplate();

    // 缓存 access_token
    private String cachedAccessToken;
    private long accessTokenExpireTime;

    /**
     * 发送下单成功通知
     */
    public void sendOrderFinishMessage(Orders order) {
        User user = order.getUser();
        Map<String, String> data = new HashMap<>();
        data.put("character_string3", order.getOrderNo());
        data.put("time7", order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        data.put("thing6", truncate(getOrderContent(order), 20));
        data.put("thing11", order.getTableNumber() != null ? order.getTableNumber() : "无");
        data.put("amount4", "¥" + order.getPayAmount().toString());

        sendMessage(user, TEMPLATE_ORDER_FINISH, data, order.getId());
    }

    /**
     * 发送订单取消通知
     */
    public void sendOrderCancelMessage(Orders order) {
        User user = order.getUser();
        Map<String, String> data = new HashMap<>();
        data.put("thing1", "308商业帝国");
        data.put("character_string3", order.getOrderNo());
        data.put("date7", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        data.put("amount4", "¥" + order.getPayAmount().toString());
        data.put("thing2", "商家或用户主动取消");

        sendMessage(user, TEMPLATE_ORDER_CANCEL, data, order.getId());
    }

    /**
     * 发送用餐提醒通知
     */
    public void sendMealRemindMessage(Orders order) {
        User user = order.getUser();
        Map<String, String> data = new HashMap<>();
        data.put("thing9", "308商业帝国");
        data.put("date1", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        data.put("thing3", truncate(getOrderContent(order), 20));
        data.put("character_string14", order.getTableNumber() != null ? order.getTableNumber() : "0");

        sendMessage(user, TEMPLATE_MEAL_REMIND, data, order.getId());
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    /**
     * 获取订单商品内容摘要
     */
    private String getOrderContent(Orders order) {
        List<OrderItem> items = orderItemDao.findByOrder(order);
        if (items.isEmpty()) return "无商品";
        return items.stream()
                .map(item -> item.getProductNameSnapshot() + "x" + item.getQuantity())
                .collect(Collectors.joining("、"));
    }

    /**
     * 发送订阅消息
     */
    private void sendMessage(User user, String templateId, Map<String, String> data, Long orderId) {
        try {
            // 1. 检查用户是否订阅了该模板
            List<SubscribeTemplate> subscriptions = subscribeTemplateDao.findByUserId(user.getId());
            boolean hasSubscription = subscriptions.stream()
                    .anyMatch(sub -> sub.getTemplateId().equals(templateId) && sub.getStatus() == 1);

            if (!hasSubscription) {
                log.info("用户 {} 未订阅模板 {}，跳过发送", user.getId(), templateId);
                return;
            }

            log.info("准备发送订阅消息: userId={}, openId={}, templateId={}, orderId={}",
                    user.getId(), user.getOpenId(), templateId, orderId);

            // 2. 获取 access_token
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("获取 access_token 失败");
                return;
            }

            // 3. 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("touser", user.getOpenId());
            requestBody.put("template_id", templateId);
            requestBody.put("page", "/pages/order-detail/order-detail?id=" + orderId);
            requestBody.put("data", convertToWxData(data));

            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            // 4. 发送请求
            String url = String.format(WX_SUBSCRIBE_MESSAGE_URL, accessToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

            String response = restTemplate.postForObject(url, entity, String.class);

            log.info("微信API响应: {}", response);

            JsonNode jsonNode = objectMapper.readTree(response);
            int errcode = jsonNode.has("errcode") ? jsonNode.get("errcode").asInt() : -1;

            if (errcode == 0) {
                log.info("订阅消息发送成功: userId={}, templateId={}", user.getId(), templateId);
            } else {
                String errmsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                log.error("订阅消息发送失败: errcode={}, errmsg={}", errcode, errmsg);
            }
        } catch (Exception e) {
            log.error("发送订阅消息异常: userId={}, templateId={}", user.getId(), templateId, e);
        }
    }

    /**
     * 获取 access_token
     */
    private String getAccessToken() {
        // 检查缓存是否有效
        if (cachedAccessToken != null && System.currentTimeMillis() < accessTokenExpireTime) {
            return cachedAccessToken;
        }

        try {
            String url = String.format(WX_ACCESS_TOKEN_URL, appid, secret);

            String response = restTemplate.getForObject(url, String.class);

            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
                log.error("获取 access_token 失败: {}", response);
                return null;
            }

            cachedAccessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            accessTokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;

            return cachedAccessToken;
        } catch (Exception e) {
            log.error("获取 access_token 异常", e);
            return null;
        }
    }

    /**
     * 转换为微信订阅消息数据格式
     */
    private Map<String, Object> convertToWxData(Map<String, String> data) {
        Map<String, Object> wxData = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            Map<String, String> value = new HashMap<>();
            value.put("value", entry.getValue());
            wxData.put(entry.getKey(), value);
        }
        return wxData;
    }
}
