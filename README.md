# 留置管理中心电子值班看板系统

## 项目简介

本系统是一个电子值班看板系统，用于管理留置管理中心的值班安排和值班记录。系统支持班次配置、排班管理、记录填写和查询、以及Word导出等功能。

## 技术栈

### 后端
- Spring Boot 2.7.18
- OpenJDK 11
- MyBatis-Plus 3.5.11
- PostgreSQL 11
- Lombok
- Hutool 5.8.27
- Apache POI 5.2.5
- Swagger 3.0.0

### 前端
- Vue 3.4.0
- Vite 5.0.0
- TypeScript 5.3.0
- Element Plus 2.5.0
- Pinia 2.1.7
- Vue Router 4.2.5
- Axios 1.6.5

## 项目结构

```
├── backend/                    # 后端项目
│   ├── src/main/java/com/duty/
│   │   ├── controller/        # 控制器层
│   │   ├── service/           # 服务层
│   │   ├── mapper/            # 数据访问层
│   │   ├── entity/            # 实体类
│   │   ├── dto/               # 数据传输对象
│   │   ├── config/            # 配置类
│   │   ├── common/            # 公共类
│   │   └── exception/         # 异常处理
│   └── src/main/resources/
│       ├── application.yml    # 配置文件
│       └── db/
│           └── schema.sql     # 数据库脚本
│   └── pom.xml                # Maven配置
│
└── fronted/                    # 前端项目
    ├── src/
    │   ├── api/               # API接口
    │   ├── views/             # 页面组件
    │   ├── components/        # 公共组件
    │   ├── router/            # 路由配置
    │   ├── layout/            # 布局组件
    │   ├── utils/             # 工具函数
    │   ├── App.vue            # 根组件
    │   └── main.ts            # 入口文件
    ├── index.html             # HTML模板
    ├── vite.config.ts         # Vite配置
    ├── tsconfig.json          # TypeScript配置
    └── package.json           # 依赖配置
```

## 核心功能

### 1. 班次配置管理
- 班次的增删改查
- 默认三个班次：夜班(00:00-08:30)、白班(08:30-18:00)、晚班(18:00-23:59)
- 支持自定义班次时间段

### 2. 排班管理
- 月度日历视图展示排班情况
- 为指定日期和班次分配值班人员
- 人员搜索（支持按姓名、部门搜索）
- 排班的增删改查

### 3. 记录模板配置
- 可配置的记录类别（留置对象、巡视检查情况等）
- 支持多种输入类型（文本框、多行文本、下拉框）
- 可设置默认值和是否必填

### 4. 值班记录管理
- 填写值班记录（关联到具体的人、日期、班次）
- 查询值班记录（支持按人员、日期筛选）
- 查看值班记录详情

### 5. Word导出
- 导出单条值班记录
- 导出某个人的值班记录（支持时间段筛选）
- 批量导出值班记录

## 数据库设计

### 核心表

1. **shift_config** - 班次配置表
2. **duty_schedule** - 排班表
3. **record_template** - 记录模板配置表
4. **duty_record** - 值班记录表
5. **duty_record_detail** - 值班记录明细表

详细表结构请参考 `backend/src/main/resources/db/schema.sql`

## 快速开始

### 环境要求

- JDK 11+
- Node.js 16+
- PostgreSQL 11+
- Maven 3.6+

### 后端启动

1. **创建数据库**
```bash
createdb duty
```

2. **执行数据库脚本**
```bash
psql -d duty -f backend/src/main/resources/db/schema.sql
```

3. **修改配置**
编辑 `backend/src/main/resources/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/duty
    username: postgres
    password: your_password
```

4. **启动后端服务**
```bash
cd backend
mvn spring-boot:run
```

后端服务启动在 http://localhost:8080/api

Swagger文档：http://localhost:8080/api/swagger-ui.html

### 前端启动

1. **安装依赖**
```bash
cd fronted
npm install
```

2. **启动开发服务器**
```bash
npm run dev
```

前端服务启动在 http://localhost:3000

## 接口文档

后端使用Swagger自动生成API文档，启动后端服务后访问：
```
http://localhost:8080/api/swagger-ui.html
```

## API接口列表

### 班次配置管理
- GET /api/shift/list/enabled - 获取所有启用的班次
- GET /api/shift/page - 分页查询班次列表
- GET /api/shift/{id} - 根据ID查询班次
- POST /api/shift - 新增班次
- PUT /api/shift - 更新班次
- DELETE /api/shift/{id} - 删除班次

### 排班管理
- GET /api/schedule/page - 分页查询排班列表
- GET /api/schedule/month/{year}/{month} - 获取月度排班表
- GET /api/schedule/date/{date} - 获取指定日期的排班列表
- POST /api/schedule/batch - 批量添加排班
- PUT /api/schedule - 更新排班
- DELETE /api/schedule/{id} - 删除排班
- PUT /api/schedule/cancel/{id} - 取消排班
- GET /api/schedule/person/search - 搜索人员

### 记录模板配置
- GET /api/template/list/enabled - 获取所有启用的模板
- GET /api/template/{id} - 根据ID查询模板
- POST /api/template - 新增模板
- PUT /api/template - 更新模板
- DELETE /api/template/{id} - 删除模板

### 值班记录管理
- GET /api/record/page - 分页查询值班记录
- GET /api/record/{id} - 根据ID查询值班记录详情
- GET /api/record/schedule/{scheduleId} - 根据排班ID查询记录
- POST /api/record - 保存值班记录
- PUT /api/record/{id} - 更新值班记录
- DELETE /api/record/{id} - 删除值班记录

### Word导出
- GET /api/export/record/{recordId} - 导出单条记录
- GET /api/export/person/{personId} - 导出个人记录
- POST /api/export/batch - 批量导出记录

## 待完善功能

### 后端
- [ ] 人员库接口集成（当前使用模拟数据）
- [ ] 权限控制集成
- [ ] 数据字典管理
- [ ] 操作日志记录

### 前端
- [ ] 值班表管理页面（月度日历视图）
- [ ] 值班记录查询页面
- [ ] 记录模板配置页面
- [ ] 数据可视化图表

## 开发说明

### 人员库接口集成

`DutyScheduleServiceImpl.searchPersons()` 方法预留了人员库接口调用逻辑：
```java
@Override
public List<PersonDTO> searchPersons(String keyword) {
    // TODO: 预留人员库接口调用逻辑
    // 当前返回模拟数据，实际使用时替换为真实的人员库接口调用
}
```

### Word模板自定义

Word导出功能在 `WordExportServiceImpl` 中实现，如需自定义导出样式，可修改该类中的Word文档生成逻辑。

## 许可证

本项目仅供内部使用。

## 联系方式

如有问题，请联系开发团队。
