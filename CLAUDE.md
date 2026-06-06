# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Restaurant ordering system: a WeChat Mini Program for customers to browse products, manage carts, and place orders, paired with an admin web dashboard for management and statistics.

## Commands

### Backend (Spring Boot / Maven)
- **Build:** `mvn clean install`
- **Run:** `mvn spring-boot:run` (port 8080)
- **Test:** `mvn test`

### Admin Dashboard (`admin/`)
- **Install:** `cd admin && npm install`
- **Dev:** `npm run dev` (Vite on port 5173, proxies `/api` to `localhost:8080`)
- **Build:** `npm run build`

### WeChat Mini Program (`vue-front/uniapp/`)
- **Install:** `cd vue-front/uniapp && npm install`
- **Dev:** `npm run dev:mp-weixin` (requires WeChat DevTools)
- **Build:** `npm run build:mp-weixin`

### Database
- MySQL on `localhost:3306`, database `restaurant_order_system`
- JPA `ddl-auto=update` auto-creates/updates schema; full DDL in `SQL.md`

## Architecture

### Three-tier structure

```
Backend (Java/Spring Boot)  <--REST/JSON-->  Admin Dashboard (Vue 3 + Element Plus)
        ^
        |
        v
WeChat Mini Program (uni-app / Vue 3 Composition API)
```

### Backend (`src/main/java/com/zcl/`)

Standard Spring Boot layered architecture: Controller → Service → Repository → Entity.

- **Controllers** split into two groups:
  - `/api/` — user-facing (WxLogin, UserInfo, ProductDisplay, ProductDetail, Cart, Order, Subscribe)
  - `/api/admin/` — admin-facing (AdminAuth, AdminDashboard, AdminUser, AdminCategory, AdminProduct, AdminOrder)
- **Auth:** Two JWT interceptors registered in `WebConfig`:
  - `JwtAuthenticationInterceptor` guards `/api/**` (excludes `/api/wx/login`)
  - `AdminAuthInterceptor` guards `/api/admin/**` (excludes `/api/admin/login`)
  - Tokens carry a `type` claim to distinguish user vs admin tokens
- **Unified response:** All endpoints return `Result<T>` with `code`, `message`, `data` fields
- **Key services:** `WxLoginService` (WeChat code→openId→JWT), `CartService`, `OrderService`, `AdminDashboardService`

### WeChat Mini Program (`vue-front/uniapp/`)

7 pages with 4-tab bottom nav (Home, Order, Bills, User). Entry point is `login` page which checks for existing token.

- API layer in `api/request.js` — wraps `uni.request`, auto-attaches Bearer token, redirects to login on 401
- Auth flow: `uni.login()` → send code to backend → receive JWT → store in `uni.setStorageSync('token')`

### Admin Dashboard (`admin/`)

Vue 3 SPA with Element Plus. Routes: login, dashboard (ECharts stats), users, categories, products (list+form), orders (list+detail), stats.

- API layer uses Axios, proxied via Vite config to `localhost:8080`
- Token stored/managed via `utils/auth.js`

### Database

13+ tables managed by JPA entities. Key relationships: Product→Category, Product↔OptionGroup (via ProductOptionRelation), Cart→CartItems, Orders→OrderItems. See `SQL.md` for full schema.

## Configuration

Backend config in `src/main/resources/application.properties` — DB credentials, WeChat appid/secret, JWT secret. Secrets support env var fallbacks (`${ENV_VAR:default}`). **Never commit real secrets.**
