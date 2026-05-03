# 项目上下文说明

这个文件用于给新的 Codex 对话快速建立项目上下文，避免每次都从零阅读整个仓库。内容基于当前仓库实现和近期讨论整理。

## 项目概览

这是一个全栈食谱分享与推荐平台，仓库根目录是 `E:\info_elte\Szakdoga`。

主要模块：

- `Recipe_BackEnd`：Java Spring Boot 后端。
- `Recipe_FrontEnd`：Vue 3 前端。
- `docker-compose.yml`：本地开发基础设施，包含 PostgreSQL、Elasticsearch、Kibana、Redis。
- `uploads`：本地上传图片目录，通过后端静态资源映射到 `/images/**`。

项目核心功能：

- 用户注册、登录、登出。
- JWT 认证 + Redis active session 校验。
- 创建、编辑、查看食谱。
- 上传食谱图片和头像图片。
- 食谱搜索、筛选、推荐。
- 收藏/取消收藏食谱。
- 食谱评分和取消评分。
- 用户偏好设置。
- flavour / cuisine 标准标签读取与 Redis 缓存。

## 技术栈

后端：

- Java 21/22 环境下开发。
- Spring Boot 3.2.5。
- Spring Security。
- Spring Data JPA。
- Spring Data Elasticsearch。
- Spring Data Redis。
- PostgreSQL。
- Redis。
- Elasticsearch 8.12.2。
- MapStruct。
- Maven wrapper：`./mvnw`。

前端：

- Vue 3。
- Vue Router。
- Element Plus。
- Axios 风格 request 封装。

本地基础设施：

- PostgreSQL：`localhost:5432`，数据库名 `recipedb`。
- Redis：`localhost:6379`。
- Elasticsearch：`localhost:9200`。
- Kibana：`localhost:5601`。

注意：用户之前明确说过不要为了 Redis 端口冲突随意改 `docker-compose.yml` 的 `6379:6379` 映射。

## 后端结构

后端包结构：

- `controller`：REST Controller。
- `service` / `service.impl`：业务逻辑。
- `repository`：JPA / Elasticsearch repository。
- `entity`：PostgreSQL JPA 实体。
- `document`：Elasticsearch document。
- `dto`：请求/响应 DTO。
- `mapper`：MapStruct mapper。
- `security`：JWT 过滤器和 Spring Security 相关类。
- `config`：Security、静态资源等配置。
- `utils`：认证、JWT 等工具类。

重要 Controller：

- `AuthController`：注册、登录、登出。
- `RecipeController`：食谱搜索入口、详情、创建/编辑、评分、推荐入口。
- `RecipeTaxonomyController`：`/api/taxonomy/flavours` 和 `/api/taxonomy/cuisines`。
- `UserController`：用户信息、收藏、头像、偏好。
- `UploadController`：图片上传。
- `GlobalExceptionHandler`：基础 validation / bad request 异常处理。

重要 Service：

- `UserServiceImpl`：注册、登录、用户信息、收藏、偏好、头像。
- `AuthSessionRedisServiceImpl`：Redis active session 管理。
- `RecipeServiceImpl`：食谱详情、创建、编辑、关系集合构建、ES 同步。
- `RecipeCardServiceImpl`：Elasticsearch 搜索、列表、推荐、trending。
- `RecipeTaxonomyServiceImpl`：flavour / cuisine 标准化、读取、Redis 缓存、cache eviction。
- `RatingServiceImpl`：评分、删除评分、更新 recipe 的平均分和评分数。
- `UploadFileServiceImpl`：图片上传校验和本地保存。

## 认证与 Session 设计

项目不是传统 `HttpSession`，而是：

`JWT authentication with Redis-backed active session validation`

实现方式：

- 登录成功时，`AuthSessionRedisServiceImpl.createSession(userId)` 生成 UUID sessionId。
- Redis key 格式是 `auth:active_session:{userId}`，TTL 24 小时。
- `UserServiceImpl.loginUser` 生成 JWT 时把 `sessionId` 写入 token claim。
- `JwtAuthenticationFilter` 每次请求解析 JWT，并检查 token 中的 `sessionId` 是否等于 Redis 中当前 active session。
- 登出时 `UserServiceImpl.deleteSession` 删除 Redis session。

效果：

- 支持 logout 后 token 失效。
- 支持 single active session control，新登录会替换旧 session。

认证模块的准确描述：

- `Implemented JWT authentication with Redis-backed active session validation for logout and single-session control.`

## Recipe DTO 设计

近期已决定并实现：把 recipe 的 request 和 response DTO 拆开。

当前设计：

- `RecipeRequestDto`：只用于 create / update 请求，只包含用户可写字段。
- `RecipeDetailDto`：只用于详情响应，可以包含服务端计算/填充字段。
- `RecipeCardDto`：用于列表卡片和搜索结果。

`RecipeRequestDto` 允许用户提交：

- `title`
- `coverImage`
- `description`
- `cookingTimeMin`
- `difficulty`
- `flavours`
- `courses`
- `cuisines`
- `dietTypes`
- `ingredients`
- `steps`

`RecipeDetailDto` 可以返回：

- `id`
- `author`
- `averageRating`
- `ratingCount`
- `userScore`
- `isLiked`
- `version`
- 以及上述 recipe 基础字段和集合字段。

目的：

- 防止 over-posting / mass assignment。
- 防止客户端提交 `averageRating`、`ratingCount`、`version`、`author`、`isLiked` 等服务端管理字段。
- 比单纯在 `RecipeDetailDto` 上加 `@Null` 更清晰、更长期可维护。

MapStruct 规则：

- `RecipeMapper.toRecipe(RecipeRequestDto)` 只映射简单字段。
- `RecipeMapper.updateRecipeFromRequest(RecipeRequestDto, @MappingTarget Recipe)` 只更新简单字段。
- 关联集合仍然在 `RecipeServiceImpl` 中构建，因为需要 repository / taxonomy service：
  - `flavours`
  - `cuisines`
  - `courses`
  - `dietTypes`
  - `ingredients`
  - `steps`

## 食谱搜索设计

搜索使用 Elasticsearch。

统一关键词搜索：

- 前端搜索栏只传一个 keyword。
- 后端使用 `RecipeCardDto.title` 作为当前 keyword 字段。
- `RecipeCardServiceImpl.getRecipeCards` 中使用 ES `multi_match`。
- 搜索字段：
  - `title^3`
  - `ingredients^2`
  - `description`

含义：

- 用户输入一个词，例如 `chicken`。
- 后端同时搜索标题、描述、食材文本。
- 标题权重最高，食材次之，描述最低。

筛选项：

- `flavours`
- `cuisines`
- `dietTypes`
- `courses`
- `minTime`
- `maxTime`

重要设计决策：

- ingredient 不做标准化、不做 Redis cache、不做 tag 体系。
- ingredient 只是食谱内容的一部分，用 ES text search 搜索。
- flavour / cuisine 才作为标准标签处理。
- course / dietType 是固定白名单，不允许用户自由创建。
- course 使用 `CourseEnum`；dietType 使用 `DietTypeEnum`，当前按前端选项支持 `Vegetarian`、`Vegan`、`Gluten-Free`、`Dairy-Free`、`Keto`、`Halal`。
- `FixedReferenceDataInitializer` 会在启动时根据 enum 补齐 `courses` 和 `diet_types` 表的固定 seed 数据。

## Taxonomy 设计

`flavour` 和 `cuisine` 由 `RecipeTaxonomyService` 统一管理。

职责：

- 标准化名称。
- 读取 flavour/cuisine 标准列表。
- Redis 缓存标准列表。
- 创建新 taxonomy value 后清理对应 cache。

Redis key：

- `recipe:taxonomy:flavours`
- `recipe:taxonomy:cuisines`

TTL：

- 6 小时。

前端行为：

- `RecipeCreate.vue` 中 cuisine/flavour 允许 `allow-create`，因为创建食谱时用户可以新增有意义的新标签。
- `profile/index.vue` 中 favorite cuisine/flavour 不允许 `allow-create`，只允许从标准列表选择。

并发处理：

- `getOrCreateFlavour` / `getOrCreateCuisine` 已改成数据库级 upsert。
- Repository 中通过 `insert ... on conflict (name) do nothing` 实现幂等插入。
- 这样两个请求同时创建同名 flavour/cuisine 时，不会因为唯一键冲突直接 500。
- 插入或忽略后再 `findByName` 回查数据库中的最终记录。

## Redis 缓存设计说明

本项目里 Redis 不应该被当成 recipe / taxonomy 的主数据库。PostgreSQL 才是 source of truth，Redis 主要用于 session、缓存和短期状态。

核心原则：

- Redis 里的缓存数据是“可丢可重建”的。
- “可丢”表示 Redis key 被删除、过期、宕机丢失时，不应该导致核心业务数据丢失。
- “可重建”表示缓存没了以后，可以从 PostgreSQL / Elasticsearch 重新查询并写回 Redis。
- 因此 Redis 缓存一般不需要像 ES 那样做可靠 outbox 同步。

当前已经适合 Redis 的地方：

- `AuthSessionRedisServiceImpl`：保存 active session，用于 JWT logout 和 single-session control。
- `RecipeTaxonomyServiceImpl`：缓存 flavour / cuisine 标准列表。

Taxonomy Redis 使用的是 cache-aside pattern：

- 读 flavour/cuisine 列表时先查 Redis。
- Redis 命中就直接返回。
- Redis 未命中就查 PostgreSQL，然后写回 Redis，并设置 TTL。
- 创建新 flavour/cuisine 后，不直接手动维护 Redis list，而是删除对应 cache key。
- 下次读取时再从 PostgreSQL 重新构建缓存。

为什么创建后删除 cache，而不是更新 Redis list：

- 删除 cache 更简单、更可靠。
- 并发创建时直接操作 Redis list 容易出现重复值、顺序混乱或漏更新。
- PostgreSQL 有唯一约束和 upsert，最终数据以 PostgreSQL 为准。

Redis 和 ES 的区别：

- ES 是搜索索引，如果不同步会导致搜索结果错误，所以使用 outbox + scheduled retry worker 做可靠最终一致性。
- Redis 多数场景是缓存，如果缓存失效，只要能从主数据源重建，就可以接受删除/过期。

大厂常见做法：

- 对 Redis cache：常用 cache-aside + TTL + 写后删除缓存。
- 对 ES / 搜索索引：常用 outbox + MQ / CDC + consumer，或本项目这种轻量 outbox + scheduled worker。
- Redis 一般不作为普通业务数据的唯一来源，除非是 session、限流计数器、排行榜、分布式锁等专门场景。

后续 Redis 可扩展场景：

- 热门 recipe / trending 结果缓存。
- 推荐结果短 TTL 缓存。
- recipe detail 短 TTL 缓存。
- 登录、上传、评分接口 rate limit。
- 防重复提交或短时间重复操作控制。
- 如果后端未来多实例部署，可用 Redis 做 scheduled worker 分布式锁，避免多个实例重复跑同一批任务。

## 推荐系统设计

推荐逻辑主要在 `RecipeCardServiceImpl`。

Hero recommendations：

- 根据当前 localHour 推断 course：
  - 5-11：BREAKFAST
  - 11-16：LUNCH
  - 16-22：DINNER
  - 其他：SNACK
- 使用 Elasticsearch function score。
- 综合 course、averageRating、ratingCount、createdAt freshness、randomScore。

Right-now / personalized recommendations：

- 使用用户偏好作为 hard filters。
- 考虑：
  - dietary
  - allergies
  - timeAvailability
- 再结合当前时间对应 course 和质量评分。

Behavior recommendations：

- 使用用户最新收藏的 recipe 作为 seed。
- 根据 seed recipe 的 cuisines、flavours、title 做加权推荐。
- 排除 seed recipe 本身。

Taste recommendations：

- 使用用户显式偏好中的 cuisines 和 flavours 做加权。

Trending recipes：

- 使用 averageRating、ratingCount、createdAt freshness 做 function score 排序。

## 上传设计

上传入口：

- `/api/upload`
- 用户头像上传在 `/api/users/avatar`，复用 `UploadFileService`。

安全边界：

- `/api/upload/**` 不应该匿名放行。
- `/images/**` 可以公开访问，因为前端需要展示图片。

`UploadFileServiceImpl` 当前做了：

- 空文件检查。
- 文件大小限制，默认 5 MB。
- MIME 检查：JPEG / PNG / WEBP。
- 扩展名检查：`.jpg`、`.jpeg`、`.png`、`.webp`。
- 使用 MD5 作为文件名，避免重复保存。
- 路径 normalize，防止路径逃逸。

配置：

- `recipe.file.upload-dir`
- `recipe.file.upload-url`
- `recipe.file.max-size-bytes`
- `spring.servlet.multipart.max-file-size`
- `spring.servlet.multipart.max-request-size`

## 重要安全与质量问题

已经处理或部分处理：

- 上传接口不应匿名开放。
- 上传文件服务端校验。
- JWT secret 不应硬编码，已改为从配置读取。
- recipe create/update 不再使用 `RecipeDetailDto` 直接接收请求，改为 `RecipeRequestDto`。
- request validation 已在部分 DTO 和 controller 上补充。
- 有 `GlobalExceptionHandler` 处理 validation / bad request。

仍需注意：

- ES 同步已改为轻量 outbox + scheduled retry worker；后续如果引入 MQ，可升级为 outbox publisher + MQ consumer。
- `course` 和 `dietType` 已改为 enum 白名单；非法请求会作为 bad request 处理。
- `RatingServiceImpl` 的平均分和评分数回写在并发下仍需谨慎。
- 部分 service/controller 仍依赖实现类而不是接口，例如 `RatingServiceImpl`。
- `RecipeController.deleteRecipe` 方法名实际删除的是 rating，命名需要后续整理。

## 前端结构和行为

前端主要目录：

- `src/views/home`
- `src/views/recipes`
- `src/views/recommend`
- `src/views/profile`
- `src/views/admin`
- `src/api`
- `src/router`
- `src/component`

关键页面：

- `recipes/index.vue`：recipe 搜索和筛选页。
- `recipes/recipe.vue`：recipe 详情页，包含评分。
- `recipes/RecipeCreate.vue`：创建/编辑 recipe。
- `profile/index.vue`：用户资料、偏好、收藏、创建的 recipe。
- `recommend/index.vue`：推荐页。
- `admin/recipes.vue`：前端 admin recipe 页面。

路由守卫：

- `requiresAuth`：需要登录。
- `requiresAdmin`：需要 admin。
- 前端从 localStorage / token 解析判断登录与 admin。

注意：

- 后端完整 admin controller 目前并不明显，描述功能时不要过度强调完整后台管理系统。

## 测试与验证命令

常用后端测试命令：

```powershell
./mvnw -q -DskipTests compile
```

```powershell
./mvnw -q "-Dtest=RecipeServiceImplTest,RecipeControllerTest" test
```

```powershell
./mvnw -q "-Dtest=RecipeServiceImplTest,RecipeControllerTest,RecipeCardServiceImplTest,RecipeTaxonomyServiceImplTest,UploadFileServiceImplTest,AuthControllerTest" test
```

注意：

- `RecipeBackEndApplicationTests` 是 `@SpringBootTest`，会尝试连接本地 PostgreSQL。
- 如果本地 PostgreSQL 没启动，这个测试可能失败并报 Hibernate dialect / JDBC metadata 错误。
- 这种失败不一定代表代码编译或单元测试有问题。

前端验证命令：

```powershell
npm run build
```

需要在 `Recipe_FrontEnd` 下运行。

## 工作注意事项

- 这个仓库经常处于 dirty worktree，不要随意 revert 用户改动。
- 用户不希望随便改 Redis Docker 端口。
- 修改行为前优先补测试，尤其是 backend service/controller 层。
- 不要把 ingredient 重新设计成标准 tag/cache，之前已经决定 ingredient 只做 ES 文本搜索。
- flavour/cuisine 是标准 taxonomy，可以缓存和标准化。
- course/dietType 是固定值，后续应偏向枚举/白名单，不应继续无限自动创建。
