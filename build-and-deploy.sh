#!/bin/bash

echo "========================================"
echo "前后端合并部署脚本"
echo "========================================"
echo

echo "[1/4] 清理旧版本..."
rm -rf backend/src/main/resources/static
echo "已删除旧的静态资源"
echo

echo "[2/4] 构建前端..."
cd fronted
npm run build
if [ $? -ne 0 ]; then
    echo "前端构建失败！"
    exit 1
fi
cd ..
echo "前端构建完成"
echo

echo "[3/4] 复制静态资源到后端..."
mkdir -p backend/src/main/resources/static
cp -r fronted/dist/* backend/src/main/resources/static/
echo "静态资源复制完成"
echo

echo "[4/4] 构建后端..."
cd backend
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "后端构建失败！"
    exit 1
fi
cd ..
echo "后端构建完成"
echo

echo "========================================"
echo "部署成功！"
echo "========================================"
echo
echo "启动方式："
echo "  cd backend"
echo "  java -jar target/duty-roster-system-1.0.0.jar"
echo
echo "访问地址：http://localhost:8080/api/"
echo "========================================"
