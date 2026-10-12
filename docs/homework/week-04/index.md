# 第 04 周作业

## 业务建模

本周围绕“设备租赁与预约管理平台”的设备查询与时间段预约场景完成业务建模，并在单体 Spring Boot 工程中实现可运行的内存版本。

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

当前版本使用内存集合保存设备和预约，以便先验证单体应用的分层结构和核心业务规则；JPA Repository 与 MySQL 持久化将在“数据库持久化”任务中接入。

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

设备创建成功后返回 HTTP 201。应用刚启动时第一台设备的 ID 为 `1`，可继续创建预约：

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

执行结果为 `BUILD SUCCESS`，共运行 5 个测试，失败 0、错误 0、跳过 0。自动化测试覆盖正常创建预约、拒绝重叠预约、允许首尾相接的预约和拒绝无效时间范围。
