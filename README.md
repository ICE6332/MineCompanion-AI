# AI Companion Mod - Fabric 模组

一个基于 Fabric 的 Minecraft AI 陪伴模组，使用 FakePlayer 技术让 AI 伙伴能够像真实玩家一样陪伴你游戏。

## 项目状态

✅ **Phase 1: 基础框架搭建 - 已完成**

- ✅ 创建项目目录结构
- ✅ 配置 Gradle 构建系统
- ✅ 配置 Fabric Mod 元数据
- ✅ 实现主 Mod 类和客户端入口

✅ **Phase 2: FakePlayer 架构重构 - 已完成**

- ✅ 集成 Carpet Mod 的 FakePlayer API
- ✅ 实现 FakePlayer 管理系统（AIFakePlayerManager）
- ✅ 实现 Controller 模式（MovementController、ViewController）
- ✅ 重写所有指令以支持 FakePlayer
- ✅ 实现自动跟随系统
- ✅ 实现视角追踪系统
- ✅ 使用 Fabric 事件系统（ServerTickEvents、ServerLifecycleEvents）
- ✅ 成功编译构建（ai-companion-mod-0.2.1.jar）

✅ **Phase 3: WebSocket 通信与状态收集 - 已完成** ⭐

**重要更新**: 实现了与 AI 服务的实时通信能力！

已完成的工作：
- ✅ WebSocket 客户端集成（Java-WebSocket 1.5.7）
- ✅ 自动重连机制（指数退避策略）
- ✅ 游戏状态收集器（玩家、AI、环境）
- ✅ 配置管理系统（JSON 持久化）
- ✅ 消息处理系统（动作指令、对话消息）
- ✅ 线程安全的消息传输
- ✅ 成功编译构建（ai-companion-mod-0.3.0.jar）

⏳ **Phase 4: AI 决策与智能系统 - 待开发**

待完成的工作：
- ⏳ AI 决策引擎（基于 LLM）
- ⏳ 主动对话系统
- ⏳ 协同活动（挖矿、建造、战斗）
- ⏳ 记忆系统（短期/长期记忆）

## 技术栈

- **Minecraft**: 1.21.10
- **Fabric Loader**: 0.17.3
- **Fabric API**: 0.134.1+1.21.10
- **Carpet Mod**: 1.21.10-1.4.188+v251016 ⭐
- **Java-WebSocket**: 1.5.7 ⭐
- **Gson**: 2.11.0
- **Fabric Loom**: 1.11.8
- **Java**: 21
- **Gradle**: 9.1.0

## 核心特性

### 🎮 FakePlayer 架构

与传统的自定义实体不同，本 Mod 使用 **FakePlayer** 技术：

- ✅ **真实玩家身份**: AI 伙伴被识别为真正的玩家
- ✅ **完整玩家能力**: 可以挖矿、建造、战斗、使用物品
- ✅ **自动渲染**: 使用 Minecraft 原生玩家模型和皮肤
- ✅ **Mod 兼容**: 其他 Mod 也会把 AI 当作真实玩家
- ✅ **服务器友好**: 可以在多人服务器使用

### 🤖 智能行为系统

- **跟随系统**: AI 会自动跟随玩家，保持合适距离
  - 距离太近：原地等待
  - 距离适中：缓慢跟随
  - 距离太远：快速追赶或传送

- **视角系统**: AI 可以持续看向指定玩家或位置
  - 支持头部和身体同步转向
  - 流畅的视角追踪

### 🌐 WebSocket 通信系统

- **实时双向通信**: 与本地 AI 服务建立 WebSocket 连接
  - 自动重连机制（指数退避策略）
  - 线程安全的消息传输
  - 连接状态监控

- **游戏状态收集**: 定期收集游戏世界信息
  - 玩家状态（位置、生命、饥饿度、游戏模式）
  - AI 伙伴状态（位置、生命、当前动作）
  - 环境状态（时间、天气、生物群系）
  - 可配置的更新频率

- **消息处理系统**: 接收并执行 AI 服务的指令
  - 动作指令（跟随、看向、移动）
  - 对话消息（AI 发送聊天消息）
  - 配置同步
  - 错误通知

### ⚙️ 配置系统

- **JSON 配置文件**: `.minecraft/config/ai-companion.json`
  - WebSocket 开关（默认关闭）
  - 服务器地址配置
  - 状态更新频率设置
  - 调试模式开关

## 项目结构

```
ai-companion-mod/
├── src/
│   ├── main/java/com/aicompanion/
│   │   ├── AICompanionMod.java              # 主 Mod 类（事件注册）
│   │   ├── player/
│   │   │   ├── AIFakePlayerManager.java     # FakePlayer 管理器
│   │   │   └── AIPlayerController.java      # FakePlayer 控制器
│   │   ├── controller/
│   │   │   ├── MovementController.java      # 移动控制
│   │   │   └── ViewController.java          # 视角控制
│   │   ├── command/
│   │   │   └── AICompanionCommand.java      # 指令系统
│   │   ├── network/                         # ⭐ 网络通信模块
│   │   │   ├── AIWebSocketClient.java       # WebSocket 客户端
│   │   │   ├── ConnectionManager.java       # 连接管理器
│   │   │   ├── MessageHandler.java          # 消息处理器
│   │   │   └── protocol/
│   │   │       └── Message.java             # 消息协议
│   │   ├── state/                           # ⭐ 状态收集模块
│   │   │   ├── GameStateCollector.java      # 游戏状态收集器
│   │   │   ├── GameStateData.java           # 游戏状态数据
│   │   │   ├── PlayerStateData.java         # 玩家状态数据
│   │   │   ├── AIStateData.java             # AI 状态数据
│   │   │   ├── EnvironmentStateData.java    # 环境状态数据
│   │   │   └── Position.java                # 位置数据
│   │   └── config/                          # ⭐ 配置模块
│   │       └── AICompanionConfig.java       # 配置管理器
│   ├── client/java/com/aicompanion/client/
│   │   └── AICompanionModClient.java        # 客户端入口
│   └── main/resources/
│       ├── fabric.mod.json                  # Mod 元数据
│       └── aicompanion.mixins.json          # Mixin 配置
├── build.gradle                              # 构建配置
├── gradle.properties                         # 版本配置
└── README.md                                 # 本文档
```

## 快速开始

### 环境要求

- **Java 21+** (推荐 Java 21)
- **Minecraft 1.21.10** (Fabric)
- **Fabric Loader 0.17.3+**
- **Fabric API 0.134.1+1.21.10**
- **Carpet Mod 1.21.10-1.4.188+** (自动包含在 Mod 中)

### 编译项目

```cmd
cd ai-companion-mod
gradlew.bat build
```

编译成功后，在 `build/libs/` 目录找到：
- `ai-companion-mod-0.3.0.jar` - 主 Mod 文件

### 安装使用

1. 确保安装了 Fabric Loader 和 Fabric API
2. 将 `ai-companion-mod-0.3.0.jar` 放入 `.minecraft/mods/` 目录
3. 启动游戏，进入单人世界或服务器
4. （可选）修改配置文件 `.minecraft/config/ai-companion.json` 以启用 WebSocket 功能

## 使用指南

### 可用指令

所有指令都需要管理员权限（OP 权限等级 2）

#### 基础管理

```
/aicompanion spawn <名字>
# 在你面前生成一个 AI 伙伴
# 例如: /aicompanion spawn 小明

/aicompanion kill <名字>
# 移除指定的 AI 伙伴
# 例如: /aicompanion kill 小明

/aicompanion list
# 列出所有当前存在的 AI 伙伴及其坐标
```

#### 行为控制

```
/aicompanion follow <名字> [玩家名]
# 让 AI 跟随玩家（不填玩家名则跟随自己）
# 例如: /aicompanion follow 小明
# 例如: /aicompanion follow 小明 Steve

/aicompanion look <名字> [玩家名]
# 让 AI 看向玩家（不填玩家名则看向自己）
# 例如: /aicompanion look 小明
# 例如: /aicompanion look 小明 Alex

/aicompanion stop <名字>
# 停止 AI 的所有行为（停止跟随和看向）
# 例如: /aicompanion stop 小明
```

### 配置文件

首次启动游戏后，Mod 会在 `.minecraft/config/ai-companion.json` 创建配置文件：

```json
{
  "websocketEnabled": false,
  "serverUrl": "ws://localhost:8080/ws",
  "stateUpdateIntervalTicks": 20,
  "collectEnvironment": true,
  "debugMode": false
}
```

配置说明：
- `websocketEnabled`: 是否启用 WebSocket 通信（默认关闭）
- `serverUrl`: AI 服务的 WebSocket 地址
- `stateUpdateIntervalTicks`: 状态更新频率（20 ticks = 1 秒）
- `collectEnvironment`: 是否收集环境信息
- `debugMode`: 调试模式（打印详细日志）

### 使用示例

#### 基础使用（手动控制）

```bash
# 1. 生成一个叫"小助手"的 AI
/aicompanion spawn 小助手

# 2. 让它跟着你走
/aicompanion follow 小助手

# 3. 让它看着你
/aicompanion look 小助手

# 4. 查看所有 AI
/aicompanion list

# 5. 停止所有行为
/aicompanion stop 小助手

# 6. 移除 AI
/aicompanion kill 小助手
```

#### 高级使用（AI 服务控制）

1. **启动本地 AI 服务**（需要单独的 Python 服务，见项目文档）
2. **修改配置文件** 启用 WebSocket：
   ```json
   {
     "websocketEnabled": true,
     "serverUrl": "ws://localhost:8080/ws"
   }
   ```
3. **重启游戏**，Mod 将自动连接到 AI 服务
4. **生成 AI 伙伴**，AI 服务将接收游戏状态并自动控制伙伴行为

## 技术架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Minecraft 游戏世界                      │
│  ┌────────────────────────────────────────────────────────┐ │
│  │           AIFakePlayerManager (管理器)                 │ │
│  │  ├─ 创建/删除 FakePlayer                              │ │
│  │  ├─ 维护 FakePlayer 映射表（线程安全）                │ │
│  │  └─ 每 Tick 更新所有 AI                               │ │
│  │      │                                                  │ │
│  │      └─> AIPlayerController (控制器)                   │ │
│  │              ├─> MovementController (移动控制)         │ │
│  │              └─> ViewController (视角控制)             │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │          GameStateCollector (状态收集器)               │ │
│  │  ├─ 收集玩家状态（位置、生命、游戏模式）              │ │
│  │  ├─ 收集 AI 状态（位置、生命、当前动作）              │ │
│  │  └─ 收集环境状态（时间、天气、生物群系）              │ │
│  └────────────────────────────────────────────────────────┘ │
│                          ↓                                   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │         ConnectionManager (连接管理器)                 │ │
│  │  ├─ WebSocket 客户端（自动重连）                      │ │
│  │  ├─ 发送游戏状态更新                                  │ │
│  │  └─ 接收 AI 指令                                       │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                          ↕ WebSocket
┌─────────────────────────────────────────────────────────────┐
│                  AI Service (Python/FastAPI)                │
│  ├─ 接收游戏状态                                            │
│  ├─ LLM 决策引擎                                            │
│  ├─ 生成动作指令                                            │
│  └─ 生成对话消息                                            │
└─────────────────────────────────────────────────────────────┘
```

### 生命周期管理

- **创建**: `EntityPlayerMPFake.createFake()` → 从服务器玩家列表获取实例
- **更新**: 每个游戏 Tick 调用 `AIPlayerController.tick()`
- **清理**: 服务器关闭时自动调用 `AIFakePlayerManager.cleanup()`

### 线程安全

- 使用 `ConcurrentHashMap` 管理多个 FakePlayer
- 支持并发创建、删除、查询操作

## 当前实现功能

### ✅ 已实现（Phase 1-3）

**核心系统**:
- ✅ FakePlayer 创建和管理
- ✅ 基于指令的 AI 控制
- ✅ 智能跟随系统（距离感知、自动传送）
- ✅ 视角追踪系统（头部+身体转向）
- ✅ 自动清理机制
- ✅ 线程安全的多 AI 管理
- ✅ Fabric 事件集成

**通信与状态** ⭐ 新增:
- ✅ WebSocket 客户端（与 AI 服务实时通信）
- ✅ 自动重连机制（指数退避策略）
- ✅ 游戏状态收集器（玩家、AI、环境）
- ✅ 消息处理系统（动作指令、对话消息）
- ✅ 配置管理系统（JSON 持久化）
- ✅ 线程安全的状态传输

### ⏳ 待实现（Phase 4+）

- ⏳ AI 决策引擎（基于 LLM）
- ⏳ 主动对话系统
- ⏳ 协同活动（挖矿、建造、战斗）
- ⏳ 记忆系统（短期/长期记忆）
- ⏳ 关系系统（亲密度、情感）

## 已知限制

1. **需要 Carpet Mod**: FakePlayer 功能依赖 Carpet Mod API
2. **服务端限制**: 某些服务器可能限制 FakePlayer 的使用
3. **性能考虑**: 过多的 AI 伙伴可能影响性能（建议 < 10 个）

## 常见问题

### Q: AI 伙伴不显示皮肤？
A: FakePlayer 默认使用 Steve 皮肤。如果想要自定义皮肤，需要创建对应名称的正版账号。

### Q: 为什么 AI 不会自动做事？
A: 需要配合本地 AI 服务使用。当前版本实现了通信基础设施，AI 决策系统需要单独部署 Python AI 服务。详见项目文档中的 AI 服务部分。

### Q: 可以在多人服务器使用吗？
A: 可以，但需要服务器安装 Fabric、Fabric API、Carpet Mod 和本 Mod。

### Q: AI 会消耗服务器资源吗？
A: FakePlayer 的资源消耗与真实玩家相当。建议根据服务器性能限制 AI 数量。

### Q: WebSocket 连接失败？
A: 检查以下几点：
1. 确认配置文件中 `websocketEnabled` 设置为 `true`
2. 确认 AI 服务已启动并监听在配置的地址和端口
3. 检查防火墙是否阻止了连接
4. 查看游戏日志获取详细错误信息

### Q: 编译失败怎么办？
A: 尝试清理缓存重新构建：
```cmd
gradlew.bat clean build
```

## 开发文档

详细的开发文档位于项目根目录的 `docs/` 目录：

- [架构设计](../docs/01-架构设计.md) - 系统整体架构
- [Fabric 模组实现](../docs/02-Fabric模组实现.md) - Mod 端实现细节
- [本地 AI 服务](../docs/03-本地AI服务.md) - AI 服务架构
- [API 接口文档](../docs/04-API接口文档.md) - WebSocket 协议
- [开发路线图](../docs/05-开发路线图.md) - 开发计划

## IDE 配置

### IntelliJ IDEA

1. File → Open → 选择 `ai-companion-mod` 目录
2. 等待 Gradle 导入完成
3. Run → Edit Configurations → Add New → Gradle
4. Tasks: `runClient`

### VS Code

1. 安装 Java Extension Pack
2. 打开项目文件夹
3. 运行 `gradlew.bat genSources vscode`
4. 使用 Run and Debug 面板启动

## 贡献指南

欢迎贡献代码！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 致谢

- [Fabric](https://fabricmc.net/) - Mod 加载器和 API
- [Carpet Mod](https://github.com/gnembon/fabric-carpet) - 提供 FakePlayer API
- [Minecraft](https://www.minecraft.net/) - 游戏本体

---

**当前版本**: 0.3.0 (WebSocket + State Collection)

**开发状态**: 🟢 活跃开发中

**最后更新**: 2025-01-13
