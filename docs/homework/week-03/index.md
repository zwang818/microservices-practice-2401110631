# 第 03 周作业：Spring Boot 基础

## 本周计划

### 项目衔接

本周继续沿用第 02 周确定的“设备租赁与预约管理平台”，不变更项目方向。后续优先实现“用户查询并预约可用设备”场景，并以设备（Equipment）和预约（Reservation）作为首批核心模型。

本周仅完成 Spring Boot 基础工程搭建和运行验证，设备查询、预约、库存锁定等业务功能只做规划，暂不编写业务实现。

### 技术要求

- 使用 Java 25、Spring Boot 4.0.x 和 Maven。
- Spring Initializr 的 Group 和 Package name 使用 `com.zjgsu.wz`。
- 在仓库根目录的 `monolith/` 中创建完整 Maven 工程。
- 使用 `application.yml` 作为应用配置文件。
- 添加 Spring Web MVC 和 Spring Boot Actuator。
- 默认使用 `8080` 端口。
- 保证在 `monolith/` 中可以执行 `./mvnw test` 和 `./mvnw spring-boot:run`。

### 计划完成内容

1. 在 `docs/project-proposal.md` 中记录项目名称、目标用户、优先业务场景和两个核心模型。
2. 在 `monolith/` 中创建 Spring Boot Maven 工程并配置 Maven Wrapper。
3. 编写 Spring Boot 启动类和 `application.yml`。
4. 添加一个简单 GET 接口 `/api/hello`，用于返回项目名称或运行消息。
5. 启用 Actuator，并验证 `/actuator/health` 返回 `UP`。
6. 保留或编写使用 `@SpringBootTest` 的 `contextLoads` 测试。
7. 执行启动命令和测试命令，记录接口响应及测试结果。
8. 将运行和测试截图保存至 `docs/homework/week-03/screenshots/`。
9. 在根目录 `README.md` 中补充运行环境、启动命令、测试命令、接口地址和当前未实现内容。

### 预期交付物

| 交付物 | 计划内容 |
| --- | --- |
| `docs/project-proposal.md` | 项目名称、目标用户、优先业务场景和两个核心模型 |
| `monolith/` | 可启动、可测试的 Spring Boot 4.0.x Maven 工程 |
| `/api/hello` | 返回项目名称或问候消息的简单 GET 接口 |
| `/actuator/health` | 返回应用健康状态的 Actuator 接口 |
| 启动测试 | 使用 `@SpringBootTest` 验证 Spring 应用上下文可以加载 |
| `docs/homework/week-03/screenshots/` | 代码、启动结果、接口响应和测试结果截图 |
| 根目录 `README.md` | Java 与 Maven 要求、启动测试命令、接口地址及当前范围说明 |

## 工程创建与运行

本周在仓库根目录的 `monolith/` 中创建了设备租赁与预约管理平台的 Spring Boot 单体工程。工程使用 Java 25、Spring Boot 4.0.8 和 Maven 3.9.16，Group 和 Package name 均为 `com.zjgsu.wz`，并加入了 Spring Web MVC 与 Spring Boot Actuator。应用统一使用 `application.yml` 配置，默认运行在 `8080` 端口。

### 编译打包

进入 `monolith/` 后执行：

```bash
./mvnw clean package -DskipTests
```

命令执行成功，Maven 输出 `BUILD SUCCESS`，并生成可执行 JAR：

```text
target/equipment-rental-0.0.1-SNAPSHOT.jar
```

### 启动命令

```bash
cd monolith
./mvnw spring-boot:run
```

应用使用 Java 25.0.4 成功启动，日志显示 Tomcat 在 `8080` 端口运行，并在 `/actuator` 路径下暴露健康检查端点。

### 问候接口

请求：

```http
GET http://localhost:8080/api/hello
```

实际响应状态为 `HTTP 200`，响应内容为：

```json
{
  "project": "设备租赁与预约管理平台",
  "message": "Spring Boot application is running"
}
```

### 健康检查

请求：

```http
GET http://localhost:8080/actuator/health
```

实际响应状态为 `HTTP 200`，响应内容为：

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

以上结果表明 Spring Boot 应用、简单 GET 接口和 Actuator 健康检查均可正常访问。验证完成后已正常停止本地应用。

## 启动测试

项目保留了使用 `@SpringBootTest` 的 `contextLoads` 测试，用于确认 Spring 应用上下文可以正常加载。

在 `monolith/` 目录执行：

```bash
./mvnw test
```

实际测试结果：

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

测试成功，说明 Spring Boot 能够识别启动配置并正常创建应用上下文。

### 当前进度

- [x] 确认继续使用设备租赁与预约管理平台。
- [x] 明确目标用户、优先业务场景和两个核心模型。
- [x] 创建并完善 `docs/project-proposal.md`。
- [x] 在本文件中记录本周计划。
- [x] 创建 `monolith/` Spring Boot 工程。
- [x] 完成简单 GET 接口和健康检查。
- [x] 完成启动测试并记录结果。
- [x] 补充运行截图与根目录 README 运行说明。
- [ ] 补充测试代码或测试通过结果截图。

## 本周实现范围

本周计划完成 Spring Boot 基础工程、应用启动、一个简单 GET 接口、Actuator 健康检查和应用上下文启动测试。暂不实现设备、预约等业务模型，也不实现完整 REST API、Service、Repository 或数据库。
