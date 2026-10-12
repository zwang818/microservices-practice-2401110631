# 第 04 周作业

## 业务建模

本周围绕“设备租赁与预约管理平台”的设备查询与时间段预约场景完成业务建模，先实现单体内存版本，再接入 JPA 与 MySQL 数据库持久化。

### 核心实体及关系

- `Equipment`（设备）：记录设备编号、名称、分类、日租金、押金、设备状态、说明和创建时间。
- `Reservation`（预约）：记录预约设备、租用人、开始时间、结束时间、预约状态和创建时间。
- 一个设备可以对应多个预约，因此 `Equipment` 与 `Reservation` 是一对多关系；每条预约只属于一个设备。

### 核心业务流程

1. 管理员创建可租赁设备，系统校验设备编号不能重复。
2. 用户通过设备接口查询设备列表或设备详情。
3. 用户选择设备和时间段后提交预约。
4. 预约服务校验设备是否存在、设备是否可用、结束时间是否晚于开始时间，以及所选时间段是否与已有有效预约冲突。
5. 校验通过后生成状态为 `CONFIRMED` 的预约，并可通过预约接口查询结果。

### 业务规则

只有状态为 `CONFIRMED` 的预约参与时间冲突判断。当已有预约满足“已有开始时间早于新预约结束时间，并且已有结束时间晚于新预约开始时间”时，判定两个时间段重叠，拒绝创建新预约并返回 HTTP 409。前一个预约的结束时间等于后一个预约的开始时间时不算冲突。

## 单体应用实现

本阶段在仓库原有的 `monolith/` Spring Boot 工程中实现设备登记与预约流程，没有重新创建工程。应用沿用基础包名 `com.zjgsu.wz`，并按照职责划分为 `model`、`dto`、`service`、`controller` 和 `exception` 子包。

### 分层设计

- Model：`Equipment`、`Reservation` 分别表示设备和预约，`EquipmentStatus`、`ReservationStatus` 表示对应的业务状态。
- DTO：创建请求和接口响应分别使用独立 DTO，避免直接将业务模型作为 HTTP 请求契约，并通过 Jakarta Validation 校验必填字段、金额和设备 ID。
- Service：`EquipmentService` 负责设备编号判重和设备查询；`ReservationService` 负责设备状态、预约时间及时间冲突校验。
- Controller：`EquipmentController`、`ReservationController` 只负责接收 HTTP 请求、调用 Service 并转换响应，不直接编写数据访问或业务判断逻辑。
- Exception：使用全局异常处理统一生成错误响应，参数错误返回 400、资源不存在返回 404、编号重复或预约冲突返回 409。

任务二阶段先使用内存集合保存设备和预约，验证单体应用的分层结构和核心业务规则。任务三已将存储替换为 JPA Repository 与 MySQL，具体设计和当前运行方式见下文“数据库持久化”。

### REST API

设备和预约分别提供创建、列表查询和详情查询，共实现 6 个与核心业务相关的 REST 接口，全部使用 `/api` 前缀。

| 功能 | 方法 | 路径 | 成功状态码 |
| --- | --- | --- | ---: |
| 创建设备 | POST | `/api/equipment` | 201 |
| 查询设备列表 | GET | `/api/equipment` | 200 |
| 查询设备详情 | GET | `/api/equipment/{id}` | 200 |
| 创建预约 | POST | `/api/reservations` | 201 |
| 查询预约列表 | GET | `/api/reservations` | 200 |
| 查询预约详情 | GET | `/api/reservations/{id}` | 200 |

创建预约时，Controller 将 `CreateReservationRequest` 交给 `ReservationService`。Service 依次检查设备是否存在、设备状态是否为 `AVAILABLE`、结束时间是否晚于开始时间，以及该设备是否已有重叠的 `CONFIRMED` 预约。全部校验通过后才创建预约并返回 `ReservationResponse`。

### 运行命令

进入单体工程并启动应用：

```bash
cd monolith
./mvnw spring-boot:run
```

终端出现 `Started EquipmentRentalApplication` 后，应用运行在 `http://localhost:8080`。可以先执行以下命令创建设备：

```bash
curl -i -X POST http://localhost:8080/api/equipment \
  -H 'Content-Type: application/json' \
  -d '{"code":"CAM-001","name":"索尼微单相机","category":"摄影设备","dailyRate":120.00,"deposit":1000.00,"description":"适合短期摄影项目"}'
```

设备创建成功后返回 HTTP 201。以下示例假设返回 ID 为 `1`；接入数据库后应使用实际返回的 ID，重启应用不会清空数据，也不会重置 ID。README 提供自动提取 ID 的示例。可继续创建预约：

```bash
curl -i -X POST http://localhost:8080/api/reservations \
  -H 'Content-Type: application/json' \
  -d '{"equipmentId":1,"renterName":"张三","startTime":"2026-10-20T09:00:00","endTime":"2026-10-21T09:00:00"}'
```

预约创建成功后返回 HTTP 201，状态为 `CONFIRMED`。设备与预约列表可通过以下命令查询：

```bash
curl -i http://localhost:8080/api/equipment
curl -i http://localhost:8080/api/reservations
```

### 业务规则验证

在已有预约时间范围内再次提交重叠预约：

```bash
curl -i -X POST http://localhost:8080/api/reservations \
  -H 'Content-Type: application/json' \
  -d '{"equipmentId":1,"renterName":"李四","startTime":"2026-10-20T12:00:00","endTime":"2026-10-21T12:00:00"}'
```

系统返回 HTTP 409，错误信息为“该设备在所选时间段已被预约”，证明时间冲突规则已经生效。若后一条预约从 `2026-10-21T09:00:00` 开始，与前一条预约首尾相接，则允许创建。

### 运行结果

实际启动应用后，健康检查返回 `UP`；创建设备和创建预约均返回 HTTP 201；设备与预约列表查询返回 HTTP 200；重复设备编号和重叠预约返回 HTTP 409；请求字段或时间范围错误返回 HTTP 400；查询不存在的资源返回 HTTP 404。

同时在 `monolith/` 目录执行：

```bash
./mvnw test
```

任务二内存版本当时的执行结果为 `BUILD SUCCESS`，共运行 5 个测试，失败 0、错误 0、跳过 0。自动化测试覆盖正常创建预约、拒绝重叠预约、允许首尾相接的预约和拒绝无效时间范围。接入数据库后的完整测试结果见下文“测试与验证”。

## 数据库持久化

### 实现思路

沿用原有设备登记与预约流程，将设备、预约的内存集合替换为 MySQL 存储。设备与预约分别映射到 `equipment`、`reservation` 两张表，预约通过 `equipment_id` 外键关联设备。字段、主键、唯一约束、非空约束、金额与时间 CHECK 约束及索引记录在 `docs/database.md`。

- 在原工程中加入 Spring Data JPA、MySQL Connector/J，以及测试使用的 H2 依赖。
- 两个模型添加 JPA 实体映射，主键使用数据库自增；预约采用单向多对一关联。
- 新增 `repository` 包，EquipmentRepository 和 ReservationRepository 继承 JpaRepository。Controller 继续通过 DTO 接收输入和生成响应。
- Service 在事务中调用 Repository；创建预约先锁定设备行，再执行有效预约重叠检查并保存，防止并发创建相同时间段的预约。
- 预约列表和详情查询显式加载设备信息，支持关闭 Open EntityManager in View。
- 主配置使用 MySQL，通过环境变量提供连接地址、用户名和密码。启动执行可重复的建表 SQL，再用 JPA 校验映射；重启保留数据。

### 数据准备与运行命令

macOS 安装和启动 MySQL：

```bash
brew install mysql@8.4
brew services start mysql@8.4
export PATH="$(brew --prefix mysql@8.4)/bin:$PATH"
```

在仓库根目录建库，然后进入工程启动应用：

```bash
mysql -u root < monolith/scripts/create-database.sql
cd monolith
./mvnw spring-boot:run
```

默认连接本机 3306 端口的 equipment_rental 数据库。MySQL 有密码时，在同一启动终端设置 DB_PASSWORD，mysql 客户端使用 `-p`。README 记录完整配置方式。

应用启动建表后，在另一个终端进入 `monolith/` 并执行：

```bash
mysql -u root equipment_rental < scripts/demo-data.sql
mysql -u root equipment_rental < scripts/demo-data.sql
mysql -u root equipment_rental -e "SELECT id, code, name, status FROM equipment; SELECT id, equipment_id, renter_name, status FROM reservation;"
curl -i http://localhost:8080/api/equipment
curl -i http://localhost:8080/api/reservations
```

演示数据脚本以 DEMO-CAM-001 编号查找设备，并检查预约时段后插入，可顺序重复执行，不会删除已有数据。自动化测试通过测试代码准备数据：RepositoryTests 直接保存设备与预约，清除持久化上下文后重新读取；Service 和 API 测试也分别创建所需设备，测试事务自动回滚。H2 测试数据库与本地 MySQL 隔离。

### 验证方式

在 `monolith/` 执行 `./mvnw test`，验证 Repository 真实读写、实体关联、编号唯一约束、预约业务规则和 API 响应。MySQL 环境中另外验证演示脚本重复执行、API 新建记录后 SQL 可查询，以及重启应用后 API 仍能查询到原有数据。

### 实际结果（2026-10-12）

- 本机通过 Homebrew 安装并启动 MySQL 8.4.11，创建 equipment_rental 数据库。应用成功连接 MySQL，自动建立 equipment 与 reservation 两张表，JPA 映射校验通过，健康检查返回 HTTP 200 和 UP。
- 原内存应用中的一台 CAM-001 设备和一条预约已通过 API 写入 MySQL，数据库查询可以读取到对应记录。
- 连续两次执行 demo-data.sql 后，DEMO-CAM-001 设备仍只有一条，对应预约仍只有一条。
- 按 README 的示例创建新设备，成功提取实际 ID 为 3，创建预约返回 HTTP 201。设备与预约的列表和详情均返回 HTTP 200；重叠预约返回 409，无效时间返回 400，设备不存在返回 404。
- 对另一台验证设备同时提交 4 条相同时间段的预约，返回 1 个 201 和 3 个 409，未出现重复生效预约。
- 正常停止并重新启动应用后，4 条设备和 4 条预约记录的完整 API 响应与重启前一致，证明数据未随应用重启丢失。
- 执行 ./mvnw test 以及 ./mvnw clean package 均成功，当前共 11 个测试（启动 1、Repository 3、Service 4、API 3），失败 0、错误 0、跳过 0。测试可以自动准备并回滚数据，独立于 MySQL 重复运行；可执行 JAR 已更新。

提交截图应展示实体和 Repository、MySQL 表结构或查询结果，以及重启后查询结果，保存到 `docs/homework/week-04/screenshots/`。代码和文档已准备，截图需根据真实运行结果补充。

## 测试与验证

### 测试目标与覆盖范围

本任务分别验证 Repository 数据读写、Service 业务规则和 API 正常及错误响应，确保设备登记与预约流程在接入数据库后仍然正确。所有自动化测试均位于 `monolith/src/test/java/com/zjgsu/wz/`。

| 测试层次 | 测试类 | 用例数 | 验证内容 |
| --- | --- | ---: | --- |
| Repository | `repository/RepositoryTests` | 3 | 保存设备与预约，flush 并清除持久化上下文后重新读取；检查实体关联和字段；验证有效预约重叠查询、相邻时段与取消状态，以及数据库编号唯一约束 |
| Service | `service/ReservationServiceTests` | 4 | 正常创建 CONFIRMED 预约、拒绝重叠预约、允许首尾相接的预约、拒绝无效时间范围 |
| API | `controller/PersistenceApiTests` | 3 | 通过 MockMvc 验证设备创建与查询、预约创建与查询、HTTP 201/200 正常响应，以及 HTTP 400/404/409 错误响应和冲突提示 |
| 应用启动 | `EquipmentRentalApplicationTests` | 1 | 验证 Spring 应用上下文、JPA 和 Repository 能正常加载 |

### 自动准备数据与重复运行

测试配置位于 `monolith/src/test/resources/application.yml`。测试使用独立 H2 内存数据库，每个 Spring 测试上下文使用随机数据库名称，通过 `ddl-auto: create-drop` 自动建表并在上下文关闭时删除表。测试不依赖本机 MySQL 服务或手工准备的演示数据。

Repository 测试在用例中创建所需设备和预约，Service 测试通过 `@BeforeEach` 创建设备，API 测试通过接口或 Service 准备数据。三类业务测试均使用 `@Transactional`，用例结束后自动回滚，避免前一个用例的数据影响后续用例。断言使用实际生成的主键，不依赖数据库 ID 固定从 1 开始。

Repository 唯一约束用例会主动插入重复编号并断言 DataIntegrityViolationException，因此日志中出现对应的唯一约束错误属于预期验证结果；以最终失败数和错误数是否为 0 判断测试是否通过。

### 运行命令

从仓库根目录进入单体工程，执行完整测试：

```bash
cd monolith
./mvnw test
```

在同一目录再次执行 `./mvnw test`，验证无需人工清理或准备数据也能重复通过。三个业务层也可以分别执行：

```bash
./mvnw -Dtest=RepositoryTests test
./mvnw -Dtest=ReservationServiceTests test
./mvnw -Dtest=PersistenceApiTests test
```

### 实际结果（2026-10-12）

已连续执行两次 `./mvnw test`，两次均成功，结果一致：

```text
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

其中 Repository 3 个、Service 4 个、API 3 个、应用启动 1 个测试全部通过。测试报告由 Maven 自动生成在 `monolith/target/surefire-reports/` 中；源码和测试配置提交到仓库，target 中的生成报告不提交。

### 截图与提交说明

作业系统上传 1—5 张 PNG、JPG 或 JPEG 截图，建议使用 Repository、Service、API 三类测试各自通过的结果，以及完整测试连续两次通过的结果，共 4 张。截图应包含运行命令、测试类或用例数量、失败与错误数，以及 BUILD SUCCESS。对应图片保存到 `docs/homework/week-04/screenshots/`。

作业系统说明：已完成 Repository 读写、Service 预约业务规则及 API 正常和错误响应测试。测试使用独立 H2 数据库，自动建表、准备数据并通过事务回滚清理。连续两次执行 ./mvnw test 均为 11 个测试全部通过，失败和错误为 0。
