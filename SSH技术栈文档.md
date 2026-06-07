# SSH 企业级项目技术栈文档

## 技术栈总览

| 层次 | 技术 | 说明 |
|------|------|------|
| 前端 | Vue3 | 前端框架 |
| 网络请求 | Axios | HTTP 客户端 |
| MVC 框架 | Struts2 | 请求分发与处理 |
| IoC 容器 | Spring | 依赖注入与事务管理 |
| ORM 框架 | Hibernate | 对象关系映射 |
| 数据库 | MySQL | 关系型数据库 |

## 请求流程

```
Vue3 (Login.vue / UserList.vue)
    │
    │  Axios.post('/user_login.action', {username, password})
    ↓
Struts2
    │
    ├── LoginInterceptor (登录拦截器)
    │
    ├── UserAction (Action层)
    │
    ↓
DTO (LoginDTO / RegisterDTO) - 接收前端参数
    │
    ↓
Service (UserService) - 业务逻辑处理
    │
    ├── @Transactional (事务管理)
    │
    ↓
DAO (UserDao) - 持久层操作
    │
    ↓
Hibernate (User.hbm.xml) - ORM 映射
    │
    ↓
MySQL 数据库

返回流程：
    │
    ↓
VO (UserVO) - 返回给前端的数据
    │
    ↓
Result<T> - 统一返回格式
    │
    ↓
PageResult<T> - 分页数据 (可选)
```

## 项目目录结构

```
src
├─ action
│   └─ UserAction.java          # Action层，处理请求
│
├─ common
│   └─ Result.java              # 统一返回对象
│
├─ dto
│   ├─ LoginDTO.java            # 登录数据传输对象
│   └─ RegisterDTO.java         # 注册数据传输对象
│
├─ vo
│   └─ UserVO.java              # 用户展示对象
│
├─ page
│   └─ PageResult.java          # 分页返回对象
│
├─ interceptor
│   └─ LoginInterceptor.java    # 登录拦截器
│
├─ entity
│   └─ User.java                # 实体对象
│
├─ dao
│   ├─ UserDao.java             # 持久层接口
│   └─ impl
│       └─ UserDaoImpl.java     # 持久层实现
│
├─ service
│   ├─ UserService.java         # 业务层接口
│   └─ impl
│       └─ UserServiceImpl.java # 业务层实现
│
├─ spring
│   └─ applicationContext.xml   # Spring配置
│
└─ struts.xml                   # Struts2配置
```

---

## 代码示例

### 一、统一返回对象 Result

企业项目不会返回 `"登录成功"` 这样的字符串，而是返回统一格式：

**返回格式：**
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
      "id": 1,
      "username": "admin"
  }
}
```

**Result.java**
```java
package com.common;

public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static Result fail(String msg) {
        Result result = new Result();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }

    // getter setter
}
```

---

### 二、DTO（数据传输对象）

DTO 负责接收前端参数，不直接暴露实体类。

**LoginDTO.java**
```java
package com.dto;

public class LoginDTO {

    private String username;
    private String password;

    // getter setter
}
```

**RegisterDTO.java**
```java
package com.dto;

public class RegisterDTO {

    private String username;
    private String password;

    // getter setter
}
```

---

### 三、VO（展示对象）

VO 负责返回给前端，**注意：password 等敏感字段不返回**。

**UserVO.java**
```java
package com.vo;

public class UserVO {

    private Long id;
    private String username;

    // getter setter
}
```

---

### 四、分页对象

企业项目列表页必备。

**PageResult.java**
```java
package com.page;

import java.util.List;

public class PageResult<T> {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<T> rows;

    // getter setter
}
```

**返回格式：**
```json
{
  "total": 100,
  "pageNum": 1,
  "pageSize": 10,
  "rows": []
}
```

---

### 五、DAO 分页查询

**UserDao.java**
```java
package com.dao;

import java.util.List;
import com.entity.User;

public interface UserDao {

    List<User> findPage(int pageNum, int pageSize);

    Long count();

    User findByUsername(String username);

    void save(User user);
}
```

**UserDaoImpl.java**
```java
package com.dao.impl;

import com.dao.UserDao;
import com.entity.User;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import java.util.List;

public class UserDaoImpl extends HibernateDaoSupport implements UserDao {

    @Override
    public List<User> findPage(int pageNum, int pageSize) {
        return (List<User>) getHibernateTemplate().execute(
            session -> {
                String hql = "from User";
                return session.createQuery(hql)
                        .setFirstResult((pageNum - 1) * pageSize)
                        .setMaxResults(pageSize)
                        .list();
            }
        );
    }

    @Override
    public Long count() {
        return (Long) getHibernateTemplate().find(
            "select count(*) from User"
        ).get(0);
    }

    @Override
    public User findByUsername(String username) {
        List<User> list = (List<User>) getHibernateTemplate()
                .find("from User where username=?", username);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void save(User user) {
        getHibernateTemplate().save(user);
    }
}
```

---

### 六、Service 事务管理

Spring 管理事务，任何一步失败自动回滚。

**UserServiceImpl.java**
```java
package com.service.impl;

import com.common.Result;
import com.dto.LoginDTO;
import com.entity.User;
import com.vo.UserVO;
import com.dao.UserDao;
import com.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public Result<UserVO> login(LoginDTO dto) {
        User user = userDao.findByUsername(dto.getUsername());

        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (!dto.getPassword().equals(user.getPassword())) {
            return Result.fail("密码错误");
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());

        return Result.success(vo);
    }

    @Override
    public Result register(RegisterDTO dto) {
        User existing = userDao.findByUsername(dto.getUsername());
        if (existing != null) {
            return Result.fail("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        userDao.save(user);

        return Result.success(null);
    }
}
```

**事务回滚示例：**
```java
@Transactional
public void batchSave(User user, Log log) {
    userDao.save(user);   // 第一步
    userDao.save(log);    // 第二步

    // 任何一步抛出 RuntimeException，自动回滚
    // throw new RuntimeException("出错了");
}
```

---

### 七、登录拦截器

企业项目必须有登录验证。

**LoginInterceptor.java**
```java
package com.interceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpSession;

public class LoginInterceptor extends AbstractInterceptor {

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        HttpSession session = ServletActionContext
                .getRequest()
                .getSession();

        Object user = session.getAttribute("loginUser");

        if (user == null) {
            return "login";  // 未登录，跳转登录页
        }

        return invocation.invoke();  // 已登录，放行
    }
}
```

---

### 八、Struts2 配置

**struts.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE struts PUBLIC
    "-//Apache Software Foundation//DTD Struts Configuration 2.5//EN"
    "http://struts.apache.org/dtds/struts-2.5.dtd">

<struts>

    <!-- 拦截器配置 -->
    <package name="default" extends="struts-default">

        <interceptors>
            <interceptor
                name="loginInterceptor"
                class="com.interceptor.LoginInterceptor"/>

            <interceptor-stack name="myStack">
                <interceptor-ref name="defaultStack"/>
                <interceptor-ref name="loginInterceptor"/>
            </interceptor-stack>
        </interceptors>

        <!-- 登录 Action（不需要拦截） -->
        <action name="user_login" class="userAction" method="login">
            <result name="success" type="json">
                <param name="root">result</param>
            </result>
            <result name="login">/login.jsp</result>
        </action>

        <!-- 用户列表（需要登录拦截） -->
        <action name="user_list" class="userAction" method="list">
            <interceptor-ref name="myStack"/>
            <result name="success" type="json">
                <param name="root">result</param>
            </result>
            <result name="login">/login.jsp</result>
        </action>

    </package>

</struts>
```

---

### 九、Action 层

**UserAction.java**
```java
package com.action;

import com.common.Result;
import com.dto.LoginDTO;
import com.dto.RegisterDTO;
import com.service.UserService;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller("userAction")
@Scope("prototype")
public class UserAction extends ActionSupport {

    @Autowired
    private UserService userService;

    private LoginDTO loginDTO;
    private RegisterDTO registerDTO;
    private Result result;

    public String login() {
        result = userService.login(loginDTO);

        // 登录成功，存入 Session
        if (result.getCode() == 200) {
            ServletActionContext.getRequest()
                .getSession()
                .setAttribute("loginUser", result.getData());
        }

        return SUCCESS;
    }

    public String register() {
        result = userService.register(registerDTO);
        return SUCCESS;
    }

    // getter setter
    public LoginDTO getLoginDTO() { return loginDTO; }
    public void setLoginDTO(LoginDTO loginDTO) { this.loginDTO = loginDTO; }
    public RegisterDTO getRegisterDTO() { return registerDTO; }
    public void setRegisterDTO(RegisterDTO registerDTO) { this.registerDTO = registerDTO; }
    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }
}
```

---

### 十、Vue3 前端调用

**Login.vue**
```vue
<template>
  <div class="login-container">
    <h2>用户登录</h2>
    <form @submit.prevent="handleLogin">
      <div>
        <label>用户名：</label>
        <input v-model="form.username" type="text" placeholder="请输入用户名" />
      </div>
      <div>
        <label>密码：</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" />
      </div>
      <button type="submit">登录</button>
    </form>
    <p v-if="errorMsg" style="color: red;">{{ errorMsg }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const form = ref({
  username: '',
  password: ''
})
const errorMsg = ref('')

const handleLogin = async () => {
  try {
    const res = await axios.post('/user_login.action', {
      username: form.value.username,
      password: form.value.password
    })

    console.log(res.data)

    if (res.data.code === 200) {
      alert('登录成功：' + res.data.data.username)
      // 跳转到用户列表页
    } else {
      errorMsg.value = res.data.msg
    }
  } catch (err) {
    errorMsg.value = '请求失败'
  }
}
</script>
```

**UserList.vue**
```vue
<template>
  <div class="user-list">
    <h2>用户列表</h2>
    <table border="1">
      <thead>
        <tr>
          <th>ID</th>
          <th>用户名</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in userList" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.username }}</td>
        </tr>
      </tbody>
    </table>

    <!-- 分页 -->
    <div class="pagination">
      <button @click="changePage(currentPage - 1)" :disabled="currentPage <= 1">
        上一页
      </button>
      <span>第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages">
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const userList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

const fetchUsers = async () => {
  const res = await axios.get('/user_list.action', {
    params: {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
  })

  if (res.data.code === 200) {
    userList.value = res.data.data.rows
    total.value = res.data.data.total
    totalPages.value = Math.ceil(total.value / pageSize.value)
  }
}

const changePage = (page) => {
  currentPage.value = page
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>
```

---

## 核心设计要点

| 要点 | 说明 |
|------|------|
| **DTO/VO 分离** | 入参和出参解耦，VO 不返回敏感字段（如密码） |
| **统一返回格式** | `Result<T>` 封装 code/msg/data，前端统一处理 |
| **声明式事务** | Spring `@Transactional` 管理事务，失败自动回滚 |
| **登录拦截器** | Struts2 拦截器实现登录验证，保护接口安全 |
| **分页查询** | `PageResult<T>` 返回 total/pageNum/pageSize/rows |

---

## 数据流向图

```
┌─────────────────────────────────────────────────────────┐
│                      前端 Vue3                          │
│  Login.vue / UserList.vue                              │
└─────────────────────┬───────────────────────────────────┘
                      │ Axios.post('/user_login.action')
                      ↓
┌─────────────────────────────────────────────────────────┐
│                    Struts2 层                           │
│  LoginInterceptor ──→ UserAction                       │
└─────────────────────┬───────────────────────────────────┘
                      │ loginDTO / registerDTO
                      ↓
┌─────────────────────────────────────────────────────────┐
│                    Service 层                           │
│  UserServiceImpl (@Transactional)                      │
└─────────────────────┬───────────────────────────────────┘
                      │ HQL 查询
                      ↓
┌─────────────────────────────────────────────────────────┐
│                     DAO 层                              │
│  UserDaoImpl (HibernateDaoSupport)                     │
└─────────────────────┬───────────────────────────────────┘
                      │ SQL
                      ↓
┌─────────────────────────────────────────────────────────┐
│                    MySQL 数据库                         │
└─────────────────────────────────────────────────────────┘

返回路径：
MySQL → Hibernate → DAO → Service → VO → Result<T> → 前端
```
