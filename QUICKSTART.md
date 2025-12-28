# 快速启动指南

## 一键启动（推荐）

### Windows用户

创建以下批处理文件：

**start-backend.bat**
```batch
@echo off
echo Starting Backend Service...
cd backend
mvn spring-boot:run
pause
```

**start-frontend.bat**
```batch
@echo off
echo Starting Frontend Service...
cd fronted
npm run dev
pause
```

### Linux/Mac用户

**start-backend.sh**
```bash
#!/bin/bash
echo "Starting Backend Service..."
cd backend
mvn spring-boot:run
```

**start-frontend.sh**
```bash
#!/bin/bash
echo "Starting Frontend Service..."
cd fronted
npm run dev
```

使用方法：
```bash
chmod +x start-backend.sh start-frontend.sh
./start-backend.sh  # 终端1
./start-frontend.sh # 终端2
```

---

## 完整启动步骤

### 第一步：数据库准备

```bash
# 1. 确保PostgreSQL已安装并启动
pg_ctl status

# 2. 创建数据库
createdb duty

# 3. 执行数据库脚本
psql -d duty -f backend/src/main/resources/db/schema.sql

# 4. 验证表是否创建成功
psql -d duty -c "\dt"
```

预期输出：
```
        List of relations
 Schema |       Name        | Type  |  Owner
--------+-------------------+-------+----------
 public | duty_record       | table | postgres
 public | duty_record_detail| table | postgres
 public | duty_schedule     | table | postgres
 public | record_template   | table | postgres
 public | shift_config      | table | postgres
```

### 第二步：启动后端

```bash
cd backend

# 首次启动，编译项目
mvn clean install

# 启动服务
mvn spring-boot:run
```

等待看到：
```
========================================
值班看板系统启动成功！
访问地址: http://localhost:8080/api
========================================
```

验证后端：
```bash
# 测试接口
curl http://localhost:8080/api/shift/list/enabled
```

应该返回默认的3个班次数据。

### 第三步：启动前端

```bash
cd fronted

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

等待看到：
```
  VITE v5.0.0  ready in XXX ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

### 第四步：访问系统

打开浏览器访问：http://localhost:3000

---

## 验证系统功能

### 1. 测试班次配置
- 点击左侧菜单"班次配置"
- 应该显示3个默认班次：夜班、白班、晚班

### 2. 测试记录模板
- 点击左侧菜单"记录模板配置"
- 应该显示5个默认模板类别

### 3. 测试添加排班
- 点击左侧菜单"值班表管理"
- 点击"添加排班"
- 选择日期和班次
- 搜索并选择人员（模拟数据：张三、李四、王五）
- 点击确定

### 4. 测试填写记录
- 在日历中找到刚才添加的排班
- 点击"填写记录"
- 填写各模板内容
- 点击保存

### 5. 测试查询和导出
- 点击左侧菜单"值班记录查询"
- 查看记录列表
- 点击"查看详情"
- 点击"导出"下载Word文档

---

## 常用命令

### 后端
```bash
# 清理编译
mvn clean

# 编译不运行
mvn install

# 跳过测试编译
mvn install -DskipTests

# 运行
mvn spring-boot:run

# 打包
mvn package
```

### 前端
```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

### 数据库
```bash
# 连接数据库
psql -d duty

# 查看所有表
\dt

# 查看表结构
\d shift_config

# 退出
\q

# 备份数据库
pg_dump duty > backup.sql

# 恢复数据库
psql duty < backup.sql
```

---

## 端口说明

| 服务 | 端口 | 用途 |
|------|------|------|
| 后端API | 8080 | Spring Boot服务 |
| 前端开发服务器 | 3000 | Vite开发服务器 |
| PostgreSQL | 5432 | 数据库 |

如果端口冲突，可以修改：
- 后端：`backend/src/main/resources/application.yml` 的 `server.port`
- 前端：`fronted/vite.config.ts` 的 `server.port`

---

## 开发模式调试

### 后端调试
使用IDE（如IntelliJ IDEA）打开backend项目：
1. 设置断点
2. Debug运行 `DutyRosterApplication`
3. 发送请求进行调试

### 前端调试
浏览器访问：http://localhost:3000
1. 按F12打开开发者工具
2. 查看Console日志
3. Network标签查看API请求
4. Vue DevTools查看组件状态

---

## 故障排查

### 问题1：后端启动失败
**症状**：`ApplicationContextException: Failed to start bean`

**解决方案**：
```bash
# 检查数据库是否启动
pg_ctl status

# 检查数据库连接
psql -d duty -c "SELECT 1"

# 查看详细错误日志
mvn spring-boot:run -X
```

### 问题2：前端无法连接后端
**症状**：Network Error 或 CORS错误

**解决方案**：
1. 确认后端已启动：`curl http://localhost:8080/api/shift/list/enabled`
2. 检查前端代理配置：`fronted/vite.config.ts`
3. 检查后端CORS配置：`backend/src/main/java/com/duty/config/CorsConfig.java`

### 问题3：数据库连接失败
**症状**：`Connection refused`

**解决方案**：
```bash
# 检查PostgreSQL服务
pg_ctl status

# 启动PostgreSQL
pg_ctl start

# 或在Windows服务中启动PostgreSQL服务
```

---

## 生产部署

### 后端打包
```bash
cd backend
mvn clean package -DskipTests
# 生成的jar文件：target/duty-roster-system-1.0.0.jar
```

运行jar文件：
```bash
java -jar target/duty-roster-system-1.0.0.jar
```

### 前端构建
```bash
cd fronted
npm run build
# 生成的静态文件：dist/
```

部署dist目录到Web服务器（如Nginx）。

---

## 下一步

启动成功后，请查看：
1. `TESTING.md` - 完整的测试指南
2. `README.md` - 项目详细文档

祝使用愉快！🎉
