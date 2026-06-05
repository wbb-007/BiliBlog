# BiliBlog

一个仿 bilibili 气质的个人博客样板项目，当前已经落成：

- `frontend`：Vue 3 + Vite + Element Plus
- `backend`：Spring Boot 3 + Java 21 + Spring Data JPA + MySQL
- `stitch_bili_style_personal_blog`：你提供的原始设计稿压缩包解压内容

## 已完成页面

- 首页 `/`
- 文章详情 `/post/:id`
- 博主空间 `/space`
- 投稿广场 `/community`（只读展示，公网版本不开放前台投稿）
- 独立管理控制台 `http://localhost:5174/admin.html`

## 已完成接口

- `GET /api/home`
- `GET /api/categories`
- `GET /api/posts/{id}`
- `GET /api/posts/{id}/comments`
- `POST /api/posts/{id}/comments`
- `GET /api/profile`
- `POST /api/posts`
- `POST /api/auth/send-code`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/reset-password`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `GET /api/admin/overview`
- `GET /api/admin/users`
- `PUT /api/admin/users/{id}`
- `DELETE /api/admin/users/{id}`
- `GET /api/admin/posts`
- `PUT /api/admin/posts/{id}`
- `DELETE /api/admin/posts/{id}`
- `GET /api/admin/announcements`
- `POST /api/admin/announcements`
- `PUT /api/admin/announcements/{id}`
- `DELETE /api/admin/announcements/{id}`

## 数据存储

后端运行环境已经切换为真正的 MySQL。

- 默认数据库名：`blogbili`
- 默认端口：`3306`
- 默认用户名：`blogbili_app`
- 密码：通过环境变量 `DB_PASSWORD` 配置，不建议写进代码仓库
- JDBC URL：
  `jdbc:mysql://127.0.0.1:3306/blogbili?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true`

如果数据库不存在，Spring Boot 会在连接时自动创建 `blogbili` 库。

测试环境仍然使用 H2 内存库，只是为了保证 `mvn test` 不依赖你本机是否已经装好 MySQL。

## 登录与管理员

- 默认管理员邮箱：`admin@biliblog.local`
- 默认管理员初始密码：通过环境变量 `BLOG_ADMIN_INITIAL_PASSWORD` 配置，首次上线请使用强密码
- 独立控制台开发地址：`http://localhost:5174/admin.html`
- 博客前台不开放用户注册登录；管理员文章发布、相册上传和站点设置都放到独立控制台中

首次启动时会自动创建管理员账号，并为它写入初始密码。

后台现在已经支持：

- 用户搜索、角色切换、启停用、删除
- 文章搜索、分类筛选、新建、编辑、删除
- 公告推送创建、修改、启停展示

## MySQL 配置

如果你想自定义数据库连接，直接配置这些环境变量即可：

```bash
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=blogbili
DB_USERNAME=blogbili_app
DB_PASSWORD=your_strong_database_password
```

如果你用的是 VSCode 的 MySQL 插件，连接信息可以这样填：

- Host：`127.0.0.1`
- Port：`3306`
- User：`blogbili_app`
- Password：填写你设置的 `DB_PASSWORD`
- Database：`blogbili`

## 邮箱验证码说明

系统现在默认走真实 SMTP 发送。

如果你没有配置 `spring.mail.*` / `MAIL_*`，验证码接口会直接返回 `503`，不会再伪造验证码完成注册或找回密码。

等你后面部署服务器时，推荐直接使用环境变量：

```bash
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=your_account@qq.com
MAIL_PASSWORD=your_smtp_auth_code
MAIL_SMTP_STARTTLS=true
BLOG_MAIL_FROM=your_account@qq.com
BLOG_ADMIN_EMAIL=your_admin@qq.com
```

也可以换成 163、企业邮箱、Gmail 或其他标准 SMTP 服务，只要提供对应主机、端口和授权码即可。

如果你只是本地联调，仍然可以手动打开开发态验证码回显：

```bash
BLOG_MAIL_DEBUG_RETURN_CODE=true
```

默认值现在是 `false`，也就是更接近真实线上行为。

## 本地启动

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`

前提是本机已经有可访问的 MySQL 服务。

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev:site
```

前端默认运行在 `http://localhost:5173`

Vite 已配置代理，请求 `/api` 时会转发到 Spring Boot。

### 3. 启动独立控制台

```bash
cd frontend
npm run dev:admin
```

独立控制台默认运行在 `http://localhost:5174/admin.html`

它和博客前台共用同一套后端接口，但界面入口已经分离，适合后面单独挂到子域名或单独路径。

## 当前数据模式

项目已移除启动时自动灌入的演示文章和演示公告。

- 首页、投稿广场、后台文章列表现在都直接读取数据库真实数据
- 公网前台只负责展示和评论，不开放用户注册、登录和投稿
- 如果后台还没有发布文章，前台会显示空态提示
- 历史演示文章如果仍是初始化那一批，会在后端启动时自动清理

## 后续建议

现在项目已经具备完整的基础博客闭环，后续如果继续扩展，建议优先考虑：

1. 给文章编辑器接 Markdown 或富文本编辑器，并增加图片上传
2. 为评论区补楼中楼回复、审核和通知
3. 给后台增加分页、操作日志和数据图表
4. 再补一套正式环境的数据库迁移脚本（Flyway / Liquibase）

## 构建验证

```bash
cd frontend && npm run build
cd backend && mvn test
```

执行 `npm run build` 后会同时产出：

- 博客前台：`dist/index.html`
- 独立控制台：`dist/admin.html`
