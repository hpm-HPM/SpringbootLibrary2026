# SpringbootLibrary2026 图书馆管理系统

基于 **Spring Boot 3 + Spring Data JPA + MySQL + Vue 3** 的前后端分离图书馆管理系统。后端提供 REST API，前端为独立的静态页面，可部署在 Tomcat 中运行，不使用 JSP。

---

## 1. 功能概览

### 角色与权限

| 功能 | 管理员 | 操作员 | 教师 | 学生 |
| --- | :---: | :---: | :---: | :---: |
| 登录、注册、退出 | ✓ | ✓ | ✓ | ✓ |
| 用户信息查询（只读、模糊搜索、详情） | ✓ |  |  |  |
| 图书查询 / 查看馆藏 | ✓ | ✓ | ✓ | ✓ |
| 添加、编辑、废弃图书 | ✓ | ✓ |  |  |
| 图书副本管理、封面/图集上传 | ✓ | ✓ |  |  |
| 借书、还书 | ✓ | ✓ | ✓ | ✓ |
| 个人借阅记录（全部 / 未还 / 日期范围） | ✓ | ✓ | ✓ | ✓ |
| 管理员借阅查询（关键字 / 超期 / 日期范围） | ✓ |  |  |  |
| 预约图书、查看我的预约 | ✓ | ✓ | ✓ | ✓ |
| 预约管理（全部预约） | ✓ |  |  |  |
| 推荐图书 |  |  | ✓ |  |
| 审阅推荐（通过 / 驳回） | ✓ |  |  |  |
| 借阅数据统计（按类型 / 按专业） | ✓ |  |  |  |
| 消息通知 | ✓ | ✓ | ✓ | ✓ |
| 操作日志（AOP 自动记录） | ✓ |  |  |  |

### 业务规则

- `book.stock` 表示**当前可借库存**；新增图书时按库存自动生成对应数量的 `bookcopy`。
- 借书：校验用户同时未还数量小于 5，选择一本未借出的副本，`stock - 1`，写入 `loan`，应还日期 = 借阅时间 + 图书可借天数。
- 还书：校验借阅人本人，写归还时间，副本恢复可借，`stock + 1`。
- 废弃图书：必须无借出副本且可借库存等于副本总数，状态改为“已废弃”，保留历史借阅记录。
- 预约：库存为 0 时允许预约；新增副本或还书使库存大于 0 时，系统通知最早预约人，预约变为“可借”，24 小时未借自动过期并顺延。
- 推荐：书名/作者命中馆藏时直接返回馆藏信息，不重复提交；否则写入待审核记录。管理员通过后自动建书并生成副本。
- 用户、图书、借阅、预约、推荐、日志列表均由服务端分页，默认每页 10 条。
- 所有写操作由 AOP 记录到 `operation_log`（操作人、模块、URI、参数、IP、耗时、成功/失败）。

---

## 2. 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端框架 | Spring Boot 3.2.5 |
| 持久层 | Spring Data JPA / Hibernate 6 |
| 数据库 | MySQL 8.x（数据库名 `librarydb`） |
| 安全 | Session + 登录拦截器 + 自定义权限注解 |
| 日志 | Spring AOP |
| 参数校验 | Jakarta Bean Validation |
| 构建 | Maven，JDK 17（本机可用 JDK 21 编译运行） |
| 前端 | HTML5 + CSS3 + Vue 3 + Axios（本地 vendor，无 CDN 依赖） |
| 前端容器 | Tomcat 9（静态文件，不使用 JSP） |

后端打包为**瘦 Jar**：`SpringbootLibrary2026.jar` 只包含本项目代码，第三方依赖位于 `target/lib/`，启动时通过 `loader.path` 加载，实现依赖与代码分离。

---

## 3. 目录结构

```text
SpringbootLibrary2026/
├─ pom.xml
├─ start-backend.bat                 # Windows 一键启动后端
├─ sql/
│  ├─ librarydb.sql                  # 原有八张表与 seed 数据
│  └─ schema.sql                     # 新增表（附件、预约、通知、操作日志）
├─ src/main/java/com/library/
│  ├─ LibraryApplication.java
│  ├─ common/                        # 统一响应、分页、异常、登录用户、权限注解
│  ├─ config/                        # 登录拦截器、权限拦截器、CORS、静态资源映射
│  ├─ controller/                    # REST Controller
│  ├─ dto/                           # 请求/响应 DTO
│  ├─ entity/                        # JPA 实体
│  ├─ mapper/                        # 实体 -> DTO
│  ├─ repository/                    # Spring Data JPA Repository
│  ├─ service/                       # 业务服务
│  └─ aop/                           # 操作日志切面
├─ src/main/resources/
│  └─ application.yml
├─ frontend/                         # 独立前端，可整体复制到 Tomcat
│  ├─ index.html
│  ├─ login.html / register.html
│  ├─ home.html / users.html / books.html
│  ├─ borrow.html / return.html / my-loans.html / loan-query.html
│  ├─ reservations.html / recommend.html / review.html
│  ├─ stats.html / notifications.html / logs.html
│  ├─ css/  js/  vendor/
│  └─ ...
├─ uploads/                          # 运行后保存封面/图集文件
└─ docs/
   ├─ ER.md                          # ER 图（Mermaid）
   └─ API.md                         # 接口清单
```

---

## 4. 数据库准备

### 4.1 环境

- MySQL 8.x，端口 `3306`
- 数据库：`librarydb`
- 账号示例：`root / hpm060612HPM`（请按本机实际修改 `application.yml`）

### 4.2 导入现有数据

项目中的 `sql/librarydb.sql` 是 MySQL 8 的数据库导出文件。先导入原有八张表和数据：

```powershell
$env:MYSQL_PWD='hpm060612HPM'
& 'D:\MySql\bin\mysql.exe' -uroot --default-character-set=utf8mb4 `
  -e "USE librarydb; SOURCE D:/LibraryDB/SpringbootLibrary2026/sql/librarydb.sql;"
```

### 4.3 创建本项目新增表

```powershell
& 'D:\MySql\bin\mysql.exe' -uroot --default-character-set=utf8mb4 `
  -e "USE librarydb; SOURCE D:/LibraryDB/SpringbootLibrary2026/sql/schema.sql;"
```

`sql/schema.sql` 会创建：

| 表 | 用途 |
| --- | --- |
| `book_attachment` | 图书封面、图集附件 |
| `reservation` | 预约记录 |
| `notification` | 消息通知 |
| `operation_log` | AOP 操作日志 |

> **注意**：现有 `book`、`user` 等表的列名是驼峰式（`createTime`、`bookId`、`isBorrowed`），因此 `application.yml` 配置了
> `spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl`。
> 若改成默认命名策略，Hibernate 会去寻找 `create_time` 等不存在的列。
>
> 现有表由 `librarydb.sql` 维护，新增表由 `sql/schema.sql` 维护；`ddl-auto` 设为 `none`，避免 Hibernate 自动改表结构。

---

## 5. 后端构建与运行

### 5.1 构建

在项目根目录执行：

```powershell
$env:JAVA_HOME='D:\jdk21'
mvn clean package -DskipTests
```

产物：

```text
target/SpringbootLibrary2026.jar          # 本项目代码（瘦 Jar）
target/SpringbootLibrary2026.jar.original # Maven 原始 Jar，可忽略
target/lib/                               # 66 个第三方依赖 Jar
```

### 5.2 启动

推荐直接双击 `start-backend.bat`，或在 PowerShell 执行：

```powershell
.\start-backend.bat
```

等价命令：

```powershell
java -Dfile.encoding=UTF-8 `
  "-Dloader.path=D:/LibraryDB/SpringbootLibrary2026/target/lib" `
  -jar "D:/LibraryDB/SpringbootLibrary2026/target/SpringbootLibrary2026.jar"
```

> **必须**带 `-Dloader.path` 指向 `target/lib`。只执行 `java -jar SpringbootLibrary2026.jar` 会因为依赖被外置而报
> `NoClassDefFoundError: org/springframework/boot/SpringApplication`。
>
> 默认端口为 **8081**（`application.yml` 中 `server.port`）。如端口被占用，可修改端口或停止占用进程。

启动成功后访问：

- 后端健康检查/当前用户：<http://127.0.0.1:8081/api/auth/me>
- 前端登录页：<http://127.0.0.1:8080/LibraryFrontend2026/login.html>

---

## 6. 前端部署

### 6.1 部署到 Tomcat

将整个 `frontend/` 目录复制到 Tomcat 的 `webapps` 下，并命名为 `LibraryFrontend2026`：

```powershell
$tomcat = 'D:\tomcat9\apache-tomcat-9.0.123'
Remove-Item "$tomcat\webapps\LibraryFrontend2026" -Recurse -Force -ErrorAction SilentlyContinue
Copy-Item 'D:\LibraryDB\SpringbootLibrary2026\frontend' `
  "$tomcat\webapps\LibraryFrontend2026" -Recurse
& "$tomcat\bin\startup.bat"
```

访问：

```text
http://127.0.0.1:8080/LibraryFrontend2026/login.html
```

### 6.2 前后端地址约定

`frontend/js/common.js` 默认：

```js
const pageHost = location.hostname || '127.0.0.1';
const API_BASE = window.LIBRARY_API_BASE ||
  (location.port === '8081'
    ? location.origin + '/api'
    : location.protocol + '//' + pageHost + ':8081/api');
```

- 前端部署在 Tomcat 8080 时，自动请求**当前页面主机**的 8081 接口：本机访问用 `localhost`/`127.0.0.1`，
  局域网访问（如 `http://192.168.1.100:8080/...`）会自动请求 `http://192.168.1.100:8081/api`。
- 如后端部署在其他机器/端口，在页面引入 `common.js` 之前设置：

```html
<script>window.LIBRARY_API_BASE = 'http://192.168.1.100:8081/api';</script>
```

后端 CORS 默认允许 `http://localhost:*`、`http://127.0.0.1:*` 和 `http://*:*`（课堂/局域网演示用），
可在 `application.yml` 的 `app.cors.allowed-origin-patterns` 中收窄。

### 6.3 运行方式

前端是纯静态页面，**不需要 Node.js 构建**，直接放在 Tomcat 的 `webapps` 下即可。Vue 3 与 Axios 已放在
`frontend/vendor/`，无外网 CDN 依赖。

---

## 7. 演示账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | admin123 |
| 操作员 | operator | operator123 |
| 学生 | student1 | student123 |
| 教师 | teacher | teacher123 |

> 密码在数据库中为明文，这是沿用原作业数据的设定；生产环境应改为 BCrypt 等哈希存储。

---

## 8. 接口清单（摘要）

统一响应：

```json
{ "code": 0, "message": "ok", "data": {} }
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录（JSON：name/password） |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/logout` | 退出 |
| GET | `/api/auth/me` | 当前登录用户，未登录 data=null |
| GET | `/api/users` | 用户分页查询（管理员） |
| GET | `/api/users/{id}` | 用户详情（管理员） |
| GET | `/api/books` | 图书分页/多条件查询 |
| GET | `/api/books/borrowable` | 可借图书 |
| GET | `/api/books/{id}` | 图书详情（含副本、附件） |
| POST | `/api/books` | 添加图书 |
| PUT | `/api/books/{id}` | 编辑图书 |
| POST | `/api/books/{id}/discard` | 废弃图书 |
| GET/POST | `/api/books/{bookId}/copies` | 副本列表 / 新增副本 |
| DELETE | `/api/books/copies/{copyId}` | 删除副本 |
| POST | `/api/books/{bookId}/attachments` | 上传封面/图集（multipart） |
| POST | `/api/loans/borrow` | 借书（JSON：bookId） |
| POST | `/api/loans/return` | 还书（JSON：copyId） |
| GET | `/api/loans/my` | 我的借阅（filter/start/end） |
| GET | `/api/loans/search` | 管理员借阅查询 |
| POST/GET | `/api/reservations`、`/api/reservations/my` | 预约 / 我的预约 |
| GET/POST | `/api/recommend/*` | 推荐、我的推荐、待审核、审阅 |
| GET | `/api/stats/types`、`/api/stats/majors` | 借阅统计 |
| GET | `/api/notifications` | 消息通知 |
| GET | `/api/logs` | 操作日志（管理员） |

完整接口与参数说明见 [`docs/API.md`](docs/API.md)。

---

## 9. 数据库设计

原有八张表：`book`、`bookcopy`、`loan`、`user`、`bookrecommend`、`knowledge`、`student`、`log`。

本项目新增四张表：

- `book_attachment`：一本书多张图片，`isCover` 标记封面，`sortOrder` 控制图集顺序。
- `reservation`：用户对某本书的预约，状态为 等待中 / 可借 / 已取消 / 已完成 / 已过期。
- `notification`：站内消息，预约可借、预约过期等。
- `operation_log`：AOP 记录每次 Controller 调用。

ER 图（Mermaid）见 [`docs/ER.md`](docs/ER.md)。

---

## 10. Git / Gitee 推送

仓库已在本地初始化，并按功能阶段完成提交（`git log --oneline` 可查看）：

```text
fix: 支持局域网访问并完善前后端地址说明
docs: 添加数据库脚本、ER 图、接口文档与运行说明
feat: 添加 Vue 3 前后端分离前端页面
feat: 实现认证、图书、借阅、预约、推荐、统计与 AOP 日志接口
chore: 初始化 Spring Boot 项目、实体与数据库映射
```

推送到 Gitee：

```bash
git remote add origin https://gitee.com/<你的账号>/SpringbootLibrary2026.git
git branch -M master
git push -u origin master
```

> Gitee 推送需要你的账号与仓库地址，代码本身不包含远程仓库凭据。
> 如果要把项目复制到新目录重新建仓，再执行 `git init` 后按功能分组 `git add`/`git commit` 即可。

---

## 11. 常见问题

1. **启动报 `NoClassDefFoundError`**
   确认使用 `start-backend.bat`，或手动加 `-Dloader.path=.../target/lib`。

2. **前端登录 401 / CORS**
   确认后端运行在 8081；本机访问 `http://127.0.0.1:8080/LibraryFrontend2026/` 或
   `http://localhost:8080/LibraryFrontend2026/`。局域网访问时前端会自动使用当前主机名请求 8081；
   跨机器/端口部署可设置 `window.LIBRARY_API_BASE`，并把前端来源加入 CORS 配置。

3. **数据库列名找不到**
   确认 `application.yml` 使用 `PhysicalNamingStrategyStandardImpl`，不要改成默认的蛇形命名策略。

4. **启动时 Hibernate 报错要改表**
   确认 `ddl-auto: none`，并先执行 `schema.sql` 创建新增表。

5. **中文乱码**
   数据库连接串已带 `characterEncoding=utf8`；命令行测试中文 JSON 建议使用 Node/浏览器，PowerShell 的
   `Invoke-RestMethod` 可能按本地代码页转码。

6. **上传图片访问 404**
   确认后端进程对 `uploads/` 有写权限，且 `WebConfig` 的 `/uploads/**` 映射生效；前端用 `Lib.fileUrl()` 拼接地址。

---

## 12. 已实现与验证情况

- [x] Maven 构建、瘦 Jar + `target/lib` 依赖分离
- [x] 登录拦截器、权限注解、401/403 JSON
- [x] 用户查询、图书管理、副本管理、封面/图集上传
- [x] 借书 / 还书 / 借阅查询 / 个人借阅
- [x] 预约、库存补充通知、预约到期调度
- [x] 推荐、审阅、自动建书
- [x] 按类型/专业统计
- [x] AOP 操作日志
- [x] 服务端分页与多条件查询
- [x] Vue 3 前端 16 个页面，Tomcat 部署运行
- [x] 数据库 seed 数据冒烟测试、四张新表及外键验证