---
# “雪球” 后端架构与文件说明
本项目基于 **Spring Boot 3 + Spring Data JPA** 构建，严格遵循《概要设计》的分层架构规范。
## 一、 文件夹（包）代表什么？
本项目采用了标准的 MVC 三层架构延伸出的“四层架构”，每个包都有明确的职责边界：
| 文件夹路径 | 全称 | 作用说明 |
| :--- | :--- | :--- |
| `controller` | 控制层 | **对外的门面**。负责接收前端发来的 HTTP 请求、参数校验（如 `@Valid`）、调用 Service 层获取数据，最后包装成统一的 `Result` 格式返回给前端。 |
| `service` / `service.impl` | 业务逻辑层 | **项目的大脑**。所有的核心规则都在这里（比如：发帖时必须生成V1版本记录、乐观锁冲突处理、权限校验）。Controller 只管调它，它不管怎么连数据库。 |
| `repository` | 数据访问层 | **和数据库打交道的底层**。继承自 `JpaRepository`，只需要定义方法名（如 `findByTitle`），Spring 就会自动帮你写 SQL，不需要写任何 XML 或实现类。 |
| `entity` | 实体层 | **数据库表的 Java 映射**。里面的每个属性就是数据库里的每个字段（如 `Post.java` 对应 `posts` 表）。 |
| `dto` | Data Transfer Object | **入参对象**。专门用来接收前端传过来的 JSON 数据（如 `PostCreateDTO`），防止恶意篡改无关字段，方便做 `@NotBlank` 校验。 |
| `vo` | Value Object | **出参对象**。专门用来组装返回给前端的数据（如 `UserVO` 隐藏了密码，只暴露用户名和头像）。 |
| `config` | 配置层 | **全局设定**。比如跨域配置（`WebMvcConfig` 允许前端 5173 端口访问）、安全规则配置（`SecurityConfig`）。 |
| `security` | 安全层 | **JWT 鉴权专属**。负责生成 Token、解析 Token、拦截请求验证登录状态。 |
| `common` | 公共层 | **全站通用工具**。比如统一返回格式 `Result`、全局异常拦截器 `GlobalExceptionHandler`。 |
---
## 二、 核心文件作用说明
### 1. 基础与通用设施 (common / config / 启动类)
* **`SnowBallApplication.java`**：项目的启动入口，包含 `main` 方法。
* **`Result.java`**：**极其重要**。统一了全站接口的返回格式 `{ code, message, data, timestamp }`，前端只需按这个格式解析。
* **`BusinessException.java`**：自定义业务异常。在 Service 层主动抛出它（如 `throw new BusinessException(403, "无权操作")`），会被全局拦截器捕获。
* **`GlobalExceptionHandler.java`**：**极其重要**。相当于一个保安，拦截所有代码报错，把丑陋的 500 错误翻译成漂亮的 JSON 格式（如自动把乐观锁异常转成 409 状态码）。
* **`WebMvcConfig.java`**：解决跨域问题，放行 Vite 前端。
* **`SecurityConfig.java`**：Spring Security 的核心配置，规定哪些接口需要登录（如发帖），哪些接口随便访（如看帖子列表）。
### 2. 用户与鉴权模块
* **`User.java`**：用户实体类，含 BCrypt 加密密码字段和角色枚举。
* **`JwtUtil.java`**：JWT 工具类，负责根据用户 ID 生成一串乱码，以及从乱码解析出用户 ID。
* **`JwtAuthenticationFilter.java`**：拦截每次请求，看请求头里带没带 Token，带了且合法就把用户 ID 存入上下文。
* **`AuthController.java`**：处理 `/api/v1/auth/login` 和 `/register`，不发 Token 不许进后续接口。
### 3. 内容管理与版本控制模块 (🔥 核心亮点)
* **`Post.java`**：帖子主表实体。**重点**：带有 `@Version` 乐观锁字段，防止两个人同时编辑覆盖。
* **`PostVersion.java`**：帖子版本快照表。每次修改帖子，旧内容都会在这里存一份（借鉴 Git 理念）。
* **`PostService.java` / `PostServiceImpl.java`**：**算法集中地**。
    * `createPost`：建帖子 + 写 V1 快照（事务保证原子性）。
    * `updatePost`：存旧内容到 Version 表 -> 更新主表 -> 触发 `@Version` 比对。
    * `rollbackPost`：将指定历史版本的内容覆盖回主表（实现时光倒流）。
* **`PostController.java`**：暴露 RESTful 接口（GET/POST/PUT/DELETE）。
### 4. 社区互动模块
* **`Tag.java` / `PostTag.java`**：标签表和多对多关联表。
* **`Comment.java`**：评论实体，通过 `parentId` 实现无限极楼中楼回复。
* **`Revision.java`**：改版（Fork/PR）实体。记录某用户对某篇文章的修改提议，包含 `vote_count` 投票数。
### 5. 衍生玩法模块
* **`StoryChain.java` / `ChainSegment.java`**：故事接龙主表与段落表。`prevSegmentId` 字段用于记录接龙的分支走向。
* **`Group.java` / `GroupMember.java`**：群组表与成员表。通过 `role` 字段区分群主和管理员。
* **`Book.java`**：图书实体，挂在个人主页下展示书单。
### 6. 辅助功能
* **`SearchController.java`**：综合搜索入口（按标题模糊查 / 按类型过滤）。
* **`HealthController.java`**：健康检查接口，运维用来判断服务是否挂掉。
* **`UserController.java`**：个人主页聚合接口，一次性查出某人的基本信息、发帖记录和书单。
