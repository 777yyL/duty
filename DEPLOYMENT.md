# 前后端合并部署指南

## 📦 部署架构

采用前后端合并部署方式：
- **后端**：Spring Boot (端口 8080)
- **前端**：构建为静态资源，由后端统一提供服务
- **访问地址**：http://localhost:8080/api/

---

## 🚀 快速部署（Windows）

### 方法一：使用自动部署脚本（推荐）

1. **双击运行**
   ```
   E:\workspace\nyjw\build-and-deploy.bat
   ```

2. **脚本会自动完成**：
   - 清理旧的静态资源
   - 构建前端（`fronted/dist`）
   - 复制前端资源到后端 `backend/src/main/resources/static/`
   - 打包后端 JAR 文件
   - 生成 `backend/target/duty-roster-system-1.0.0.jar`

3. **启动服务**
   ```bash
   cd backend
   java -jar target/duty-roster-system-1.0.0.jar
   ```

4. **访问系统**
   ```
   http://localhost:8080/api/
   ```

---

### 方法二：手动部署步骤

#### 第一步：构建前端

```bash
cd E:\workspace\nyjw\fronted
npm run build
```

构建完成后会在 `fronted/dist` 目录生成静态文件。

#### 第二步：复制到后端

```bash
# 创建 static 目录
mkdir E:\workspace\nyjw\backend\src\main\resources\static

# 复制所有文件
xcopy /s /e /i /y E:\workspace\nyjw\fronted\dist\* E:\workspace\nyjw\backend\src\main\resources\static\
```

**目录结构应该是：**
```
backend/src/main/resources/static/
├── index.html
└── assets/
    ├── index-xxx.js
    ├── index-xxx.css
    └── ...
```

#### 第三步：启动后端

```bash
cd E:\workspace\nyjw\backend
mvn spring-boot:run
```

或者打包后运行：

```bash
mvn clean package -DskipTests
java -jar target/duty-roster-system-1.0.0.jar
```

#### 第四步：访问系统

打开浏览器访问：
- **前端页面**：http://localhost:8080/api/
- **API文档**：http://localhost:8080/api/swagger-ui.html

---

## 🚀 快速部署（Linux/Mac）

### 使用自动部署脚本

```bash
# 给脚本添加执行权限（首次运行）
chmod +x build-and-deploy.sh

# 运行部署脚本
./build-and-deploy.sh
```

### 手动部署

```bash
# 1. 构建前端
cd fronted
npm run build

# 2. 复制到后端
mkdir -p ../backend/src/main/resources/static
cp -r dist/* ../backend/src/main/resources/static/

# 3. 启动后端
cd ../backend
mvn spring-boot:run
```

---

## 📁 部署后的目录结构

```
E:\workspace\nyjw\
├── backend/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── db/
│   │   │   └── schema.sql
│   │   └── static/              # 前端静态资源（前端构建生成）
│   │       ├── index.html
│   │       └── assets/
│   │           ├── index-xxx.js
│   │           ├── index-xxx.css
│   │           └── ...
│   └── target/
│       └── duty-roster-system-1.0.0.jar
│
├── fronted/
│   ├── src/
│   ├── dist/                    # 前端构建输出（临时）
│   └── package.json
│
├── build-and-deploy.bat         # Windows 自动部署脚本
├── build-and-deploy.sh          # Linux/Mac 自动部署脚本
└── DEPLOYMENT.md                # 本文档
```

---

## 🔧 技术实现说明

### 1. 静态资源配置

**application.yml：**
```yaml
spring:
  web:
    resources:
      static-locations: classpath:/static/,file:./static/
      cache:
        period: 3600
```

### 2. 前端路由处理

**WebMvcConfig.java：**
- 处理前端 Vue Router 的路由
- 所有非 API 请求都转发到 `index.html`
- API 请求由 Controller 处理

### 3. 请求路径说明

| 路径类型 | 示例 | 处理方式 |
|---------|------|---------|
| 前端页面 | `/api/`, `/api/schedule` | 返回 `index.html` |
| 静态资源 | `/api/assets/index-xxx.js` | 返回静态文件 |
| API接口 | `/api/shift/list/enabled` | Controller 处理 |
| API文档 | `/api/swagger-ui.html` | Swagger 处理 |

---

## 🌐 访问地址

部署成功后，所有功能都在同一个端口：

| 功能 | 地址 |
|------|------|
| **前端应用** | http://localhost:8080/api/ |
| **班次配置** | http://localhost:8080/api/#/shift |
| **值班表** | http://localhost:8080/api/#/schedule |
| **值班记录** | http://localhost:8080/api/#/record |
| **模板配置** | http://localhost:8080/api/#/template |
| **Swagger文档** | http://localhost:8080/api/swagger-ui.html |
| **API根路径** | http://localhost:8080/api/ |

---

## ⚠️ 注意事项

### 1. context-path 配置

由于后端配置了 `context-path: /api`，所以：
- 所有请求都带 `/api` 前缀
- 前端和后端共享这个前缀
- 访问根路径 `/api/` 会返回前端页面

### 2. 重新构建前端

修改前端代码后需要：
```bash
# 重新构建前端
cd fronted
npm run build

# 复制到后端
xcopy /s /e /i /y dist\* ..\backend\src\main\resources\static\

# 重启后端
```

### 3. 开发模式 vs 生产模式

**开发模式（前后端分离）：**
- 前端：`npm run dev` → http://localhost:3000
- 后端：`mvn spring-boot:run` → http://localhost:8080/api
- 通过 Vite proxy 转发 API 请求

**生产模式（合并部署）：**
- 前端构建为静态资源，放在后端 `static/` 目录
- 只启动后端服务，统一通过 8080 端口访问

---

## 🔄 更新部署流程

### 更新前端代码

```bash
cd fronted
npm run build
xcopy /s /e /i /y dist\* ..\backend\src\main\resources\static\
# 后端会自动热重载静态资源
```

### 更新后端代码

```bash
cd backend
mvn spring-boot:run
# 或重新打包
mvn clean package -DskipTests
java -jar target/duty-roster-system-1.0.0.jar
```

### 同时更新前后端

```bash
# 运行自动部署脚本
build-and-deploy.bat
```

---

## 🐛 常见问题

### 问题1：访问 404

**症状**：访问 http://localhost:8080/api/ 显示 404

**解决方案**：
1. 检查 `backend/src/main/resources/static/` 目录是否存在
2. 检查 `static/` 目录下是否有 `index.html` 文件
3. 查看后端日志，确认静态资源路径是否正确

### 问题2：API 请求失败

**症状**：前端页面显示但 API 调用失败

**解决方案**：
1. 确认后端服务已启动
2. 检查浏览器控制台的网络请求
3. 确认请求 URL 是否正确（应该包含 `/api` 前缀）

### 问题3：刷新页面 404

**症状**：刷新页面时显示 404

**解决方案**：
1. 检查 `WebMvcConfig.java` 是否正确配置
2. 确认 `index.html` 在 `static/` 目录的根目录
3. 查看后端日志，确认路由转发是否正常

### 问题4：样式或资源加载失败

**症状**：页面显示但样式错乱

**解决方案**：
1. 检查 `static/assets/` 目录是否存在
2. 确认所有 JS、CSS 文件都已复制
3. 清除浏览器缓存重新加载

---

## 📝 生产环境部署建议

### 1. 使用 Nginx（推荐）

对于生产环境，建议使用 Nginx：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /path/to/backend/src/main/resources/static;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 2. 使用独立的前端服务器

- 前端：Nginx/Apache 托管静态文件
- 后端：Spring Boot 只提供 API
- 通过反向代理整合

### 3. Docker 部署

创建 `Dockerfile`：
```dockerfile
FROM maven:3.8-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY fronted/dist static/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## ✅ 部署检查清单

部署完成后，检查以下项目：

- [ ] 访问 http://localhost:8080/api/ 显示前端页面
- [ ] 页面样式正常，无 404 错误
- [ ] 点击各个菜单可正常切换
- [ ] 班次配置功能正常
- [ ] 值班表功能正常
- [ ] 值班记录功能正常
- [ ] Word 导出功能正常
- [ ] Swagger 文档可访问
- [ ] 刷新页面不会 404
- [ ] 前端路由正常工作

---

**部署成功后，系统即可投入生产使用！** 🎉
