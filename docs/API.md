# REST API 清单

- 基础地址：`http://127.0.0.1:8081/api`
- 认证方式：登录成功后由服务端写入 `JSESSIONID`，浏览器/前端请求需携带 Cookie（Axios `withCredentials: true`）。
- 统一响应：`{ "code": 0, "message": "ok", "data": ... }`；业务错误 `code=1`，未登录 HTTP 401，无权限 HTTP 403。
- 分页参数：`page` 从 1 开始，`size` 默认 10；分页响应 `{ content, total, page, size, totalPages }`。

## 认证

| 方法 | 路径 | 说明 | 请求 |
| --- | --- | --- | --- |
| POST | `/auth/login` | 登录 | JSON `{name, password}` |
| POST | `/auth/register` | 注册 | JSON，见下 |
| POST | `/auth/logout` | 退出 | 无 |
| GET | `/auth/me` | 当前用户 | 未登录时 `data:null` |

注册字段：

```json
{
  "name": "张三",
  "password": "123456",
  "phone": "13800000000",
  "role": "学生",
  "major": "计算机科学与技术",
  "grade": "2022级",
  "college": "计算机学院"
}
```

`role` 支持中文（管理员/操作员/教师/学生）或英文（admin/operator/teacher/student）。管理员额外字段
`permissionLevel`、`entryTime`；操作员 `area`、`duty`；学生 `grade`、`college`；教师 `college`、`title`。

## 用户（管理员）

| 方法 | 路径 | 参数 | 说明 |
| --- | --- | --- | --- |
| GET | `/users` | `page,size,keyword,role,major` | 分页；`keyword` 模糊匹配姓名/手机号/专业；创建时间倒序 |
| GET | `/users/{id}` | - | 用户详情（含角色扩展字段、权限数组） |
| GET | `/users/majors` | - | 专业列表 |

## 图书

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/books` | 登录 | `page,size,keyword,type,status,author,publisher`，多条件组合 |
| GET | `/books/borrowable` | 登录 | 可借图书（正常且 stock>0） |
| GET | `/books/types` | 登录 | 全部类型 |
| GET | `/books/{id}` | 登录 | 返回 `{book, copies, attachments}` |
| POST | `/books` | 添加书籍 | 新增图书；按 stock 自动生成副本 |
| PUT | `/books/{id}` | 编辑图书 | 只更新 title/author/type/publisher/days/price/introduction/tags |
| POST | `/books/{id}/discard` | 废弃图书 | 无借出副本且 stock=副本总数时置“已废弃” |
| GET | `/books/{bookId}/copies` | 图书副本编辑 | 副本列表 |
| POST | `/books/{bookId}/copies` | 图书副本编辑 | 新增副本，stock+1 |
| DELETE | `/books/copies/{copyId}` | 图书副本编辑 | 删除未借出且无借阅历史的副本，stock-1 |
| POST | `/books/{bookId}/attachments` | 编辑图书 | multipart：`files`（可多个）、`cover`（true/false） |
| DELETE | `/books/attachments/{attachmentId}` | 编辑图书 | 删除图片 |
| POST | `/books/attachments/{attachmentId}/cover` | 编辑图书 | 设为封面 |

图书请求示例：

```json
{
  "title": "深入理解计算机系统",
  "author": "Randal E. Bryant",
  "type": "计算机",
  "publisher": "机械工业出版社",
  "days": 30,
  "price": 139.0,
  "stock": 3,
  "introduction": "计算机系统经典教材",
  "tags": "计算机,系统,教材"
}
```

## 借阅

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/loans/borrow` | 借书 | JSON `{bookId}`；同时未还最多 5 本 |
| POST | `/loans/return` | 还书 | JSON `{copyId}`；只能还本人借阅 |
| GET | `/loans/my` | 登录 | `page,size,filter=all|unreturned,start,end` |
| GET | `/loans/search` | 借阅记录查询 | `page,size,keyword,status,overdue,start,end` |

`overdue=true` 表示应还日期早于当前时间且尚未归还；`start/end` 为借阅日期范围（含首尾）。

## 预约

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/reservations` | 预约图书 | JSON `{bookId}`；库存必须为 0，不能重复预约 |
| DELETE | `/reservations/{id}` | 预约图书 | 取消本人“等待中/可借”预约 |
| GET | `/reservations/my` | 登录 | `page,size,status` |
| GET | `/reservations/search` | 预约管理 | `page,size,status,keyword` |

库存补充后，系统把最早“等待中”预约置为“可借”，生成通知并设置 24 小时有效期；定时任务每 10 分钟清理过期预约。

## 推荐与审阅

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| POST | `/recommend/check` | 推荐图书 | JSON `{bookName,author,publisher,price}`；命中馆藏返回 `exists=true`，否则创建待审核 |
| GET | `/recommend/mine` | 推荐图书 | 我的推荐列表 |
| GET | `/recommend/pending` | 审阅推荐图书 | 待审核列表 |
| POST | `/recommend/review` | 审阅推荐图书 | `{id,approve,type,days,stock,reason}`；通过自动建书+副本 |

## 统计

| 方法 | 路径 | 权限 | 说明 |
| --- | --- | --- | --- |
| GET | `/stats/types` | 借阅数据统计 | 按图书类型统计，返回 `[{name,count}]`，全部类型 |
| GET | `/stats/majors` | 借阅数据统计 | 按用户专业统计 |

## 通知

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/notifications` | 分页 `page,size` |
| GET | `/notifications/unread` | 未读列表 |
| GET | `/notifications/unread-count` | 未读数量 |
| POST | `/notifications/{id}/read` | 标记单条已读 |
| POST | `/notifications/read-all` | 全部已读 |

## 日志（管理员）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/logs` | 分页 `page,size,module,keyword`；记录 AOP 操作日志 |

## 错误示例

```json
{
  "code": 1,
  "message": "每位读者最多同时借阅 5 本，请先归还部分图书",
  "data": null
}
```

HTTP 状态码：

- `200`：业务成功或业务校验失败（后者 `code=1`）
- `401`：未登录 / Session 过期
- `403`：已登录但无权限
- `500`：未处理的服务端异常