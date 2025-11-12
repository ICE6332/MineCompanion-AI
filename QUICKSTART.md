# 快速开始指南

## 📋 当前状态

✅ **Fabric 模组框架已搭建完成！**

已创建的文件：
```
ai-companion-mod/
├── gradle/wrapper/           ✅ Gradle Wrapper
├── src/                      ✅ 源代码结构
│   ├── main/java/           ✅ 主 Mod 类
│   ├── main/resources/      ✅ 元数据和资源
│   └── client/java/         ✅ 客户端入口
├── build.gradle             ✅ 构建配置
├── gradle.properties        ✅ 版本配置
├── settings.gradle          ✅ Gradle 设置
├── gradlew.bat              ✅ Gradle 启动器
├── build-mod.bat            ✅ 便捷构建脚本
├── README.md                ✅ 项目说明
├── .gitignore               ✅ Git 忽略配置
└── LICENSE                  ✅ MIT 许可证
```

## 🚀 下一步操作

### 方式 1: 使用便捷脚本（推荐）

1. **打开命令提示符**，切换到项目目录：
   ```cmd
   cd G:\Minecraft\Project\ai-companion-mod
   ```

2. **运行构建脚本**：
   ```cmd
   build-mod.bat
   ```
   
3. **选择操作**：
   - 首次运行：选择 `5` (完整构建)
   - 后续开发：根据需要选择对应选项

### 方式 2: 手动执行命令

#### 步骤 1: 生成 Minecraft 源码
```cmd
cd G:\Minecraft\Project\ai-companion-mod
gradlew.bat genSources
```
⏱️ 预计时间：5-15 分钟

#### 步骤 2: 编译项目
```cmd
gradlew.bat build
```
⏱️ 预计时间：1-3 分钟

#### 步骤 3: 运行测试
```cmd
gradlew.bat runClient
```
🎮 会启动一个带有你的 Mod 的 Minecraft 客户端

## ✅ 验证 Mod 是否成功加载

启动游戏后，在日志中查找：
```
[aicompanion/AICompanionMod] ========================================
[aicompanion/AICompanionMod] AI Companion Mod is initializing...
[aicompanion/AICompanionMod] Version: 0.1.0
[aicompanion/AICompanionMod] ========================================
[aicompanion/AICompanionMod] AI Companion Mod initialized successfully!
```

或者在游戏中：
1. 进入主菜单
2. 点击 "Mods" 按钮
3. 查找 "AI Companion Mod"

## 📝 重要提醒

### ⚠️ Java 版本
确保使用 **Java 21**：
```cmd
java -version
```
应该显示 `java version "21.x.x"`

### ⚠️ 首次运行
- 第一次运行 `genSources` 需要下载大量文件
- 建议在网络良好的环境下进行
- 可能需要科学上网以加快下载速度

### ⚠️ 常见问题

**问题：下载缓慢**
- 解决：配置 Gradle 镜像（见 README.md）

**问题：编译错误**
- 解决：运行 `gradlew.bat clean` 后重试

**问题：Java 版本错误**
- 解决：设置 JAVA_HOME 环境变量指向 JDK 21

## 📚 开发资源

### 项目文档
- [架构设计](../Project/docs/01-架构设计.md) - 系统整体设计
- [Fabric 模组实现](../Project/docs/02-Fabric模组实现.md) - 详细代码示例
- [本地 AI 服务](../Project/docs/03-本地AI服务.md) - AI 服务设计
- [API 接口文档](../Project/docs/04-API接口文档.md) - 通信协议
- [开发路线图](../Project/docs/05-开发路线图.md) - 完整开发计划

### 官方文档
- [Fabric Wiki](https://fabricmc.net/wiki/)
- [Fabric API Docs](https://fabricmc.net/develop/)
- [Minecraft Wiki](https://minecraft.wiki/)

## 🎯 完成框架搭建后的下一步

### Phase 2: 核心行为系统（预计 2-3 周）

需要实现：
1. **AI 实体创建**
   - 创建 `AICompanionEntity` 类
   - 实现实体注册
   - 添加基础属性和行为

2. **智能跟随系统**
   - 实现 `SmartFollowGoal` 
   - 距离分层跟随逻辑
   - 自然的随机行为

3. **WebSocket 通信**
   - 集成 WebSocket 客户端
   - 实现消息序列化
   - 建立与 AI 服务的连接

4. **游戏状态收集**
   - 收集玩家状态
   - 收集环境信息
   - 定期发送状态更新

### 准备开发环境

推荐使用 **IntelliJ IDEA**：
1. File → Open → 选择 `ai-companion-mod` 目录
2. 等待 Gradle 同步完成
3. 运行配置会自动创建

## 🎉 恭喜！

你已经成功搭建了 Fabric 模组的基础框架！

现在可以：
- ✅ 编译和运行 Mod
- ✅ 在游戏中看到 Mod 加载
- ✅ 开始实现 AI 功能

---

**需要帮助？**
- 查看 [README.md](README.md) 获取更多信息
- 查看 [开发路线图](../Project/docs/05-开发路线图.md) 了解下一步
- 参考 [Fabric 模组实现文档](../Project/docs/02-Fabric模组实现.md) 查看代码示例
