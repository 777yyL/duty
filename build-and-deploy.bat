@echo off
echo ========================================
echo 前后端合并部署脚本
echo ========================================
echo.

echo [1/4] 清理旧版本...
if exist "backend\src\main\resources\static" (
    rmdir /s /q "backend\src\main\resources\static"
    echo 已删除旧的静态资源
)
echo.

echo [2/4] 构建前端...
cd fronted
call npm run build
if %errorlevel% neq 0 (
    echo 前端构建失败！
    pause
    exit /b 1
)
cd ..
echo 前端构建完成
echo.

echo [3/4] 复制静态资源到后端...
if not exist "backend\src\main\resources\static" (
    mkdir "backend\src\main\resources\static"
)
xcopy /s /e /y /i "fronted\dist\*" "backend\src\main\resources\static\"
echo 静态资源复制完成
echo.

echo [4/4] 构建后端...
cd backend
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 后端构建失败！
    pause
    exit /b 1
)
echo 后端构建完成
cd ..
echo.

echo ========================================
echo 部署成功！
echo ========================================
echo.
echo 启动方式：
echo   cd backend
echo   java -jar target\duty-roster-system-1.0.0.jar
echo.
echo 访问地址：http://localhost:8080/api/
echo ========================================
pause
