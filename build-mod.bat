@echo off
echo ========================================
echo MineCompanion-BOT - 构建脚本
echo ========================================
echo.

echo 检查 Java 版本...
java -version
echo.

if "%1"=="setup" goto setup
if "%1"=="build" goto build
if "%1"=="run" goto run
if "%1"=="clean" goto clean
goto menu

:menu
echo 请选择操作:
echo 1. 初始化项目 (生成源码)
echo 2. 编译项目
echo 3. 运行客户端
echo 4. 清理构建
echo 5. 完整构建 (清理 + 生成源码 + 编译)
echo.
set /p choice=请输入选项 (1-5):

if "%choice%"=="1" goto setup
if "%choice%"=="2" goto build
if "%choice%"=="3" goto run
if "%choice%"=="4" goto clean
if "%choice%"=="5" goto full
goto invalid

:setup
echo.
echo [1/1] 正在生成 Minecraft 源码...
echo 这可能需要 5-15 分钟，请耐心等待...
call gradlew.bat genSources
if errorlevel 1 (
    echo.
    echo ❌ 生成源码失败！
    pause
    exit /b 1
)
echo.
echo ✅ 源码生成完成！
pause
exit /b 0

:build
echo.
echo [1/1] 正在编译项目...
call gradlew.bat build
if errorlevel 1 (
    echo.
    echo ❌ 编译失败！
    pause
    exit /b 1
)
echo.
echo ✅ 编译完成！
echo 输出文件: build\libs\minecompanion-bot-0.3.0.jar
pause
exit /b 0

:run
echo.
echo [1/1] 正在启动 Minecraft 客户端...
echo 请在游戏日志中查找 "MineCompanion-BOT" 相关信息
call gradlew.bat runClient
pause
exit /b 0

:clean
echo.
echo [1/1] 正在清理构建缓存...
call gradlew.bat clean
if errorlevel 1 (
    echo.
    echo ❌ 清理失败！
    pause
    exit /b 1
)
echo.
echo ✅ 清理完成！
pause
exit /b 0

:full
echo.
echo 开始完整构建流程...
echo.
echo [1/3] 清理旧的构建...
call gradlew.bat clean
echo.
echo [2/3] 生成 Minecraft 源码...
echo 这可能需要 5-15 分钟，请耐心等待...
call gradlew.bat genSources
if errorlevel 1 (
    echo.
    echo ❌ 生成源码失败！
    pause
    exit /b 1
)
echo.
echo [3/3] 编译项目...
call gradlew.bat build
if errorlevel 1 (
    echo.
    echo ❌ 编译失败！
    pause
    exit /b 1
)
echo.
echo ========================================
echo ✅ 完整构建成功！
echo ========================================
echo 输出文件: build\libs\minecompanion-bot-0.3.0.jar
echo.
pause
exit /b 0

:invalid
echo.
echo ❌ 无效的选项！
pause
goto menu

