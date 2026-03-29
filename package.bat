@echo off
chcp 65001 >nul
echo === PicMark 打包工具 (Windows) ===
echo.

echo 1. 清理并编译项目...
call mvn clean compile -q
if errorlevel 1 goto error

echo 2. 打包 JAR...
call mvn package -DskipTests -q
if errorlevel 1 goto error

echo 3. 清理旧的可执行文件...
if exist target\dist rmdir /s /q target\dist
mkdir target\dist

echo 4. 准备依赖库...
if exist target\libs rmdir /s /q target\libs
mkdir target\libs
copy target\picmark-1.0-SNAPSHOT.jar target\libs\

echo 5. 生成本地可执行文件...
jpackage ^
    --name "PicMark" ^
    --app-version "1.0.0" ^
    --vendor "PicMark" ^
    --input target\libs ^
    --main-jar picmark-1.0-SNAPSHOT.jar ^
    --main-class com.example.demo.Launcher ^
    --dest target\dist ^
    --type exe ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --java-options "-Djava.awt.headless=false" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --win-console false

if errorlevel 1 goto error

echo.
echo 打包完成！
echo 安装包位置: target\dist\PicMark-1.0.0.exe
echo.
goto end

:error
echo.
echo 打包失败！请检查错误信息。
exit /b 1

:end
echo === 打包流程结束 ===
pause
