#!/bin/bash

# PicMark 打包脚本 - 生成本地可执行文件
# 支持平台: macOS, Linux, Windows (通过 WSL 或 Git Bash)

set -e

echo "=== PicMark 打包工具 ==="
echo ""

# 检测操作系统
OS="unknown"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="mac"
elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
    OS="windows"
fi

echo "检测到操作系统: $OS"
echo ""

# 清理并编译
echo "1. 清理并编译项目..."
mvn clean compile -q

# 打包 JAR
echo "2. 打包 JAR..."
mvn package -DskipTests -q

# 清理旧的可执行文件目录
echo "3. 清理旧的可执行文件..."
rm -rf target/dist
mkdir -p target/dist

# 创建 libs 目录，只包含必要的 jar
echo "4. 准备依赖库..."
mkdir -p target/libs
cp target/picmark-1.0-SNAPSHOT.jar target/libs/

# 根据平台使用 jpackage
echo "5. 生成本地可执行文件..."

case $OS in
    "mac")
        # macOS: 生成 .app 应用包
        jpackage \
            --name "PicMark" \
            --app-version "1.0.0" \
            --vendor "PicMark" \
            --input target/libs \
            --main-jar picmark-1.0-SNAPSHOT.jar \
            --main-class com.example.demo.Launcher \
            --dest target/dist \
            --type app-image \
            --java-options "-Dfile.encoding=UTF-8" \
            --mac-package-identifier com.example.picmark \
            --java-options "-Dprism.order=sw" \
            --java-options "-Djava.awt.headless=false"
        echo ""
        echo "✅ 打包完成！"
        echo "可执行文件位置: target/dist/PicMark.app"
        echo "运行方式: open target/dist/PicMark.app"
        ;;
    
    "linux")
        # Linux: 生成可执行文件和 deb 包
        jpackage \
            --name "PicMark" \
            --app-version "1.0.0" \
            --vendor "PicMark" \
            --input target/libs \
            --main-jar picmark-1.0-SNAPSHOT.jar \
            --main-class com.example.demo.Launcher \
            --dest target/dist \
            --type deb \
            --java-options "-Dfile.encoding=UTF-8" \
            --java-options "-Djava.awt.headless=false" \
            --linux-shortcut \
            --linux-menu-group "PicMark"
        echo ""
        echo "✅ 打包完成！"
        echo "安装包位置: target/dist/picmark_1.0.0-1_amd64.deb"
        echo "安装命令: sudo dpkg -i target/dist/picmark_1.0.0-1_amd64.deb"
        ;;
    
    "windows")
        # Windows: 生成 .exe 安装包
        jpackage \
            --name "PicMark" \
            --app-version "1.0.0" \
            --vendor "PicMark" \
            --input target/libs \
            --main-jar picmark-1.0-SNAPSHOT.jar \
            --main-class com.example.demo.Launcher \
            --dest target/dist \
            --type exe \
            --java-options "-Dfile.encoding=UTF-8" \
            --java-options "-Djava.awt.headless=false" \
            --win-dir-chooser \
            --win-menu \
            --win-shortcut \
            --win-console false
        echo ""
        echo "✅ 打包完成！"
        echo "安装包位置: target/dist/PicMark-1.0.0.exe"
        ;;
    
    *)
        # 未知系统，尝试通用打包
        echo "未知操作系统，尝试通用打包方式..."
        jpackage \
            --name "PicMark" \
            --app-version "1.0.0" \
            --vendor "PicMark" \
            --input target/libs \
            --main-jar picmark-1.0-SNAPSHOT.jar \
            --main-class com.example.demo.Launcher \
            --dest target/dist \
            --java-options "-Dfile.encoding=UTF-8"
        echo "打包完成，请检查 target/dist 目录"
        ;;
esac

echo ""
echo "=== 打包流程结束 ==="
