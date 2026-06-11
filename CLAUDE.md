# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Restaurant ordering system — a WeChat Mini Program for customers to browse products, manage carts, and place orders, paired with an admin web dashboard for management and statistics.

**This is NOT a Spring Boot project.** It uses the classic **SSH stack: Spring 5 + Struts2 + Hibernate 5**, packaged as a WAR and deployed to Tomcat. Java target: 11.

## Commands

### Backend (SSH / Maven / Tomcat)
- **Build:** `mvn clean install`
- **Run:** `mvn tomcat7:run` (port 8080, context path `/restaurant-order`)
- **Test:** `mvn test`

### Admin Dashboard (`admin/`)
- **Install:** `cd admin && npm install`
- **Dev:** `npm run dev` (Vite on port 5173, proxies specific API prefixes to `localhost:8080/restaurant_order_war_exploded`)
- **Build:** `npm run build`

### WeChat Mini Program (`vue-front/uniapp/`)
- **Install:** `cd vue-front/uniapp && npm install`
- **Dev:** `npm run dev:mp-weixin` (requires WeChat DevTools)
- **Build:** `npm run build:mp-weixin`

### Database
- MySQL on `localhost:3306`, database `restaurant_order_system`
- Hibernate `hbm2ddl.auto=update` auto-creates/updates schema; full DDL in `SQL.md`

## Architecture

### Three-tier structure

```
Backend (SSH)  <--JSON over *.action-->  Admin Dashboard (Vue 3 + Element Plus)
      ^
      |
      v
WeChat Mini Program (uni-app / Vue 3 Composition API)
```

### Request lifecycle

```
web.xml (Struts2 filter on *.action)
  → struts.xml (action mapping + interceptor stack)
    → Action (reads JSON from request via ObjectMapper, returns NONE)
      → Service (@Service, @Transactional)
        → Dao (extends BaseDao, uses SessionFactory.getCurrentSession())
          → MySQL
```

### Backend (`src/main/java/com/zcl/`)

No `@SpringBootApplication` main class. Application boots via `web.xml` → `ContextLoaderListener` → `applicationContext.xml`.

| Layer | Package | Pattern |
|-------|---------|---------|
| **Actions** | `com.zcl.action` | Struts2 `ActionSupport` subclasses. Manually parse JSON from `HttpServletRequest` via Jackson `ObjectMapper`, call service, write `Result<T>` JSON response. Each method returns `NONE`. |
| **Services** | `com.zcl.service` | `@Service` + `@Transactional`. Business logic. |
| **DAOs** | `com.zcl.dao` | Extend `BaseDao<T, ID>` which wraps `SessionFactory.getCurrentSession()`. Provides `findById`, `findAll`, `save`, `delete`, `findByHql`, `findOneByHql`, `count`. |
| **Entities** | `com.zcl.entity` | JPA annotations (`@Entity`, `@Table`) + Lombok `@Data`. 16 tables. |
| **DTOs** | `com.zcl.dto` | Request/response objects. `Result<T>` is the unified response wrapper (`code`, `message`, `data`). |
| **Interceptors** | `com.zcl.interceptor` | `LoginInterceptor` (user JWT), `AdminInterceptor` (admin JWT). |
| **Config** | `com.zcl.config` | `CorsFilter` (allows all origins). |

### Auth pattern

Two interceptor stacks defined in `struts.xml`:

- **`userStack`**: `LoginInterceptor` validates Bearer JWT → extracts `userId` into request attribute. Used by user-facing actions.
- **`adminStack`**: `AdminInterceptor` validates admin JWT (claim `type=admin`). Used by admin-facing actions.
- Unauthenticated endpoints: `api_wx_login.action` (WeChat login), `api_admin_login.action` (admin login).
- JWT claims: user tokens carry `openId`, `userId`, `nickName`; admin tokens carry `adminId`, `username`, `role`, `type`.

### URL routing

Struts2 filter is mapped to `*.action` in web.xml. Action names are defined in `struts.xml`, e.g.:
- `api_wx_login.action` → `WxLoginAction.login()`
- `api_product_display.action` → `ProductAction.display()`
- `api_admin_products.action` → `AdminProductAction` (admin namespace)

### WeChat Mini Program (`vue-front/uniapp/`)

8 pages with 4-tab bottom nav (Home, Order, Bills, User): `index`, `order`, `bill`, `user`, `login`, `product-detail`, `order-detail`, `points`. Entry point is `login` page which checks for existing token.

- API layer in `api/request.js` — wraps `uni.request`, auto-attaches Bearer token, redirects to login on 401
- Auth flow: `uni.login()` → send code to backend → receive JWT → store in `uni.setStorageSync('token')`
- Backend URL configured in `utils/config.js`
- Static assets synced via `sync-static.ps1` (PowerShell)

### Admin Dashboard (`admin/`)

Vue 3 SPA with Element Plus. Routes: login, dashboard (ECharts stats), users, categories, products (list+form), orders (list+detail), dining tables.

- API layer uses Axios (`api/request.js`), Bearer token injection, 401 redirect
- Token stored/managed via `utils/auth.js`
- Vite dev server proxies specific API prefixes (`/api_admin`, `/api_wx`, `/api_user`, `/api_product`, `/api_cart`, `/api_order`, `/api_points`, `/api_subscribe`) and static paths (`/qrcode`, `/upload`) to `http://localhost:8080/restaurant_order_war_exploded`

## Configuration files

| File | Purpose |
|------|---------|
| `src/main/resources/applicationContext.xml` | Spring beans: component scan, HikariCP datasource, Hibernate SessionFactory, transaction manager, JwtUtil bean |
| `src/main/resources/struts.xml` | All Struts2 action mappings and interceptor stacks |
| `src/main/resources/db.properties` | DB credentials, Hibernate dialect, JWT secret, WeChat appid/secret |
| `src/main/webapp/WEB-INF/web.xml` | Servlet config: Spring listener, encoding filter, CORS filter, Struts2 filter (`*.action`) |
| `admin/vite.config.js` | Admin dev server + API proxy config |
| `vue-front/uniapp/utils/config.js` | Mini program backend base URL |

## Database

16 entity tables managed by Hibernate. Key relationships: Product→Category, Product↔OptionGroup (via ProductOptionRelation), Cart→CartItems, Orders→OrderItems. Connection pool is HikariCP (max 10). Full DDL in `SQL.md`.

## Additional References

- `QUICK_START.md` — login flow quick start guide (note: references Spring Boot in places, but the actual architecture is SSH)
- `COMPLETE_LOGIN_FLOW.md` — detailed WeChat login + JWT auth flow documentation
- `SSH技术栈文档.md` — SSH stack technical documentation
- `web/WEB-INF/web.xml` — skeleton web.xml (not used at runtime); real one is `src/main/webapp/WEB-INF/web.xml`
- `upload/` — runtime directory for user-uploaded files (product images, etc.)
