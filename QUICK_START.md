# 🚀 微信小程序 JWT Token 认证 - 快速开始指南

## ✅ 已完成的后端功能

### 1. 核心功能
- ✅ 微信登录接口（`POST /api/wx/login`）
- ✅ JWT Token 生成与验证
- ✅ 用户信息管理
- ✅ Token 自动验证拦截器
- ✅ 数据库用户存储

### 2. 新增文件清单

#### 工具类
- `src/main/java/com/zcl/util/JwtUtil.java` - JWT Token 工具类

#### DTO
- `src/main/java/com/zcl/dto/WxLoginResponse.java` - 登录响应对象

#### 配置类
- `src/main/java/com/zcl/config/JwtAuthenticationInterceptor.java` - Token 验证拦截器
- `src/main/java/com/zcl/config/WebConfig.java` - Web 配置（注册拦截器）

#### 服务类
- `src/main/java/com/zcl/service/UserInfoService.java` - 用户信息服务

#### 控制器
- `src/main/java/com/zcl/controller/UserInfoController.java` - 用户信息接口（示例）

#### 文档
- `COMPLETE_LOGIN_FLOW.md` - 完整的登录流程文档
- `QUICK_START.md` - 本文件

---

## 📦 第一步：重新加载 Maven 依赖

在 IntelliJ IDEA 中：
1. 右键点击 `pom.xml`
2. 选择 `Maven` → `Reload Project`
3. 等待依赖下载完成（特别是 jjwt 相关依赖）

或者使用命令行：
```bash
mvn clean install
```

---

## ⚙️ 第二步：检查配置文件

确认 `src/main/resources/application.properties` 包含以下配置：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_order_system
spring.datasource.username=root
spring.datasource.password=123456

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# 微信小程序配置（替换为你的真实 appid 和 secret）
wx.mini.appid=wx60b0706df54d5f2e
wx.mini.secret=4dbfdde78a84540e8ee3b780c5fd2ad0

# JWT 配置
jwt.secret=mySecretKeyForJwtTokenGenerationAndValidation123456789
jwt.expiration=604800000
```

⚠️ **重要**：生产环境必须修改 `jwt.secret` 为强随机字符串！

---

## 🗄️ 第三步：创建数据库表

虽然 JPA 可以自动建表，但建议手动创建以确保正确性：

```sql
CREATE DATABASE IF NOT EXISTS restaurant_order_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE restaurant_order_system;

CREATE TABLE `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    open_id VARCHAR(100) UNIQUE NOT NULL COMMENT '微信用户唯一标识',
    nick_name VARCHAR(50) COMMENT '用户昵称',
    avatar_url VARCHAR(255) COMMENT '用户头像URL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_open_id (open_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户表';
```

---

## ▶️ 第四步：启动后端服务

### 方式一：使用 IntelliJ IDEA
1. 找到 `TestloginApplication.java`
2. 点击运行按钮或按 `Shift + F10`

### 方式二：使用 Maven 命令
```bash
mvn spring-boot:run
```

启动成功后，应该看到类似输出：
```
Started TestloginApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

---

## 🧪 第五步：测试接口

### 使用 Postman 测试登录接口

1. 创建新请求
2. 方法：`POST`
3. URL：`http://localhost:8080/api/wx/login`
4. Body 选项卡选择 `raw` 和 `JSON`
5. 输入测试数据：

```json
{
  "code": "test_code_123",
  "nickName": "测试用户",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

6. 点击 Send

**预期响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "openId": "oXXXX...",
    "nickName": "测试用户",
    "avatarUrl": "https://example.com/avatar.jpg"
  }
}
```

### 测试需要 Token 的接口

1. 复制上一步返回的 `token`
2. 创建新请求
3. 方法：`GET`
4. URL：`http://localhost:8080/api/user/info`
5. Headers 添加：
   ```
   Authorization: Bearer <your_token_here>
   ```
6. 点击 Send

**预期响应：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "openId": null,
    "nickName": "测试用户",
    "avatarUrl": "https://example.com/avatar.jpg",
    "createTime": "2026-05-01T14:30:00",
    "updateTime": "2026-05-01T14:30:00"
  }
}
```

---

## 📱 第六步：前端集成（微信小程序）

### 1. 创建登录页面

**pages/login/login.wxml**
```html
<view class="login-container">
  <view class="logo">
    <image src="/images/logo.png" mode="aspectFit"></image>
  </view>
  
  <view class="title">欢迎使用</view>
  
  <button 
    class="login-btn" 
    bindtap="handleWechatLogin"
    disabled="{{loading}}"
  >
    {{loading ? '登录中...' : '微信授权登录'}}
  </button>
</view>
```

**pages/login/login.js**
```javascript
Page({
  data: {
    loading: false
  },

  handleWechatLogin() {
    this.setData({ loading: true });

    // 获取 code
    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          wx.showToast({ title: '获取 code 失败', icon: 'none' });
          this.setData({ loading: false });
          return;
        }

        // 获取用户信息
        wx.getUserProfile({
          desc: '用于完善用户资料',
          success: (userRes) => {
            this.doLogin({
              code: loginRes.code,
              nickName: userRes.userInfo.nickName,
              avatarUrl: userRes.userInfo.avatarUrl
            });
          },
          fail: () => {
            wx.showToast({ title: '需要授权才能使用', icon: 'none' });
            this.setData({ loading: false });
          }
        });
      }
    });
  },

  doLogin(loginData) {
    wx.request({
      url: 'http://localhost:8080/api/wx/login',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: loginData,
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          const { token, userId, openId, nickName, avatarUrl } = res.data.data;

          // 保存到本地缓存
          wx.setStorageSync('token', token);
          wx.setStorageSync('userInfo', { userId, openId, nickName, avatarUrl });

          wx.showToast({ title: '登录成功', icon: 'success' });

          // 跳转到首页
          setTimeout(() => {
            wx.switchTab({ url: '/pages/index/index' });
          }, 1500);
        } else {
          wx.showToast({ title: res.data.message || '登录失败', icon: 'none' });
        }
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  }
});
```

### 2. 在首页使用 Token

**pages/index/index.js**
```javascript
Page({
  data: {
    userInfo: null
  },

  onLoad() {
    this.loadUserInfo();
  },

  loadUserInfo() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }

    wx.request({
      url: 'http://localhost:8080/api/user/info',
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        if (res.statusCode === 200) {
          this.setData({ userInfo: res.data.data });
        } else if (res.statusCode === 401) {
          wx.reLaunch({ url: '/pages/login/login' });
        }
      }
    });
  }
});
```

---

## 🔍 常见问题排查

### 问题 1: 编译错误 "Cannot resolve symbol 'JwtUtil'"

**解决方案：**
1. 重新加载 Maven 项目
2. 清理并重新构建：`mvn clean compile`
3. 重启 IDE

### 问题 2: 运行时错误 "ClassNotFoundException: io.jsonwebtoken.Jwts"

**解决方案：**
1. 确认 `pom.xml` 中已添加 jjwt 依赖
2. 执行 `mvn dependency:resolve`
3. 重新运行应用

### 问题 3: 数据库连接失败

**解决方案：**
1. 确认 MySQL 服务已启动
2. 检查 `application.properties` 中的数据库配置
3. 确认数据库 `restaurant_order_system` 已创建

### 问题 4: 微信接口调用失败 "invalid code"

**解决方案：**
1. code 只能使用一次，每次都要重新获取
2. code 有效期只有 5 分钟
3. 确保 appid 和 secret 配置正确
4. 需要在真实的微信小程序环境中测试

### 问题 5: Token 验证失败 401

**解决方案：**
1. 确认请求头中包含 `Authorization: Bearer <token>`
2. 检查 Token 是否过期（默认 7 天）
3. 查看后端日志确认具体错误原因

---

## 📊 项目结构总览

```
testlogin/
├── src/main/java/com/zcl/
│   ├── config/
│   │   ├── JwtAuthenticationInterceptor.java  # Token 验证拦截器
│   │   └── WebConfig.java                      # Web 配置
│   ├── controller/
│   │   ├── WxLoginController.java              # 登录接口
│   │   └── UserInfoController.java             # 用户信息接口
│   ├── service/
│   │   ├── WxLoginService.java                 # 登录服务
│   │   └── UserInfoService.java                # 用户信息服务
│   ├── repository/
│   │   └── WxUserRepository.java               # 数据访问层
│   ├── entity/
│   │   └── WxUser.java                         # 用户实体
│   ├── dto/
│   │   ├── WxLoginRequest.java                 # 登录请求
│   │   ├── WxLoginResponse.java                # 登录响应
│   │   └── Result.java                         # 统一响应
│   └── util/
│       └── JwtUtil.java                        # JWT 工具类
├── src/main/resources/
│   └── application.properties                  # 配置文件
└── pom.xml                                     # Maven 依赖
```

---

## 🎯 下一步工作

### 立即可做
1. ✅ 测试登录接口是否正常
2. ✅ 编写前端登录页面
3. ✅ 实现 Token 自动刷新机制

### 后续优化
- [ ] 实现 Refresh Token 机制
- [ ] 添加 Token 黑名单功能
- [ ] 实现单设备登录限制
- [ ] 添加登录日志记录
- [ ] 实现权限控制（RBAC）
- [ ] 添加 API 限流保护

---

## 📞 获取帮助

如果遇到问题：
1. 查看后端控制台日志
2. 查看微信小程序控制台日志
3. 阅读完整文档：`COMPLETE_LOGIN_FLOW.md`
4. 检查数据库中的数据是否正确

---

## ✨ 总结

现在您的后端已经完整实现了：
1. ✅ 微信授权登录
2. ✅ JWT Token 生成
3. ✅ Token 自动验证
4. ✅ 用户信息管理
5. ✅ 完整的错误处理

接下来只需要：
1. 前端调用登录接口
2. 保存 Token 到本地缓存
3. 后续请求携带 Token
4. 处理 Token 过期情况

祝您开发顺利！🚀

---

**最后更新**: 2026-05-01  
**版本**: v1.0.0
