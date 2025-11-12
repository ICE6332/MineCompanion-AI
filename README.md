# MineCompanion-BOT

> 基于 LLM 控制的定制BOT MOD 

MineCompanion-BOT 是一款基于 Fabric 的 Minecraft 模组，利用 FakePlayer 技术与本地 AI 服务进行通讯，让AI可以驱动BOT并进行交互。

## ✨ 功能亮点
- **真实陪伴体验**：AI 以 FakePlayer 身份加入游戏世界，拥有原版玩家的全部交互能力。
- **智能行为系统**：跟随、视角追踪、闲置动作等行为可根据距离与场景自动切换。
- **实时通信**：内置 WebSocket 客户端，支持连接本地或远程 AI 服务，安全可控。
- **可扩展配置**：支持自定义 WebSocket、更新频率、调试日志等参数，方便集成自己的 AI 服务。

## 🧱 安装要求
- **Minecraft**：1.21.1（Fabric）
- **Java**：17 或 21（推荐 21）
- **Fabric Loader**：≥ 0.17.3
- **必装依赖**：Fabric API、Carpet Mod（提供 FakePlayer API）
- **可选依赖**：Cloth Config（未来 GUI 配置）、任意兼容的 AI 服务端

## ⚡ 快速开始
1. **下载模组**
   - 从 Releases 或构建产物中获取 `MineCompanion-BOT.jar`，放入 `.minecraft/mods/`。
2. **准备依赖**
   - 确保 Fabric Loader、Fabric API、Carpet Mod 已安装到同一实例。
3. **启动游戏**
   - 第一次运行会在 `.minecraft/config/ai-companion.json` 生成配置文件。
4. **启动 AI 服务**
   - 运行MineCompanion-BOT 或你自己的服务，确保 WebSocket 地址与配置保持一致。
   - 默认端口为localhost:8080。
5. **召唤 AI BOT**
   - 进入单人或服务器世界，执行 `/aicompanion spawn <AI-name>` 即可创建 AI 伙伴。

## 🛠 配置说明
配置文件位于 `.minecraft/config/ai-companion.json`，主要字段：

| 字段 | 说明 | 默认值 |
| --- | --- | --- |
| `websocketEnabled` | 是否启用与 AI 服务的实时通信 | `false` |
| `serverUrl` | WebSocket 服务地址（如 `ws://127.0.0.1:8000/ws`） | `""` |
| `stateUpdateIntervalTicks` | 状态同步周期（游戏 Tick） | `20` |
| `collectEnvironment` | 是否发送天气、生物群系等环境信息 | `true` |
| `debugMode` | 输出额外日志，便于排查连接问题 | `false` |

修改后可在游戏内通过 `/aic reload` 重新加载配置，无需重启客户端。

## 🎮 游戏内指令速览
| 指令 | 功能 |
| --- | --- |
| `/aicompanion spawn <AI-name>` | 召唤或刷新 AI 伙伴 |
| `/aicompanion follow <AI-name> <player>` | 指定跟随目标玩家 |
| `/aicompanion stop` | 停止AI行为 |
| `/aicompanion look <AI-name>` | 让 AI 盯住玩家或坐标 |
| `/aicompanion list` | 查看  AI-bot 列表 |
| `/aicompanion kill <AI-name>` | 移除 AI-bot |

> 更多开发者指令、行为脚本示例见 `docs/README-dev.md`。

## ❓ 常见问题
- **AI 不会说话或执行指令？**
  - 确认已启用 `websocketEnabled`，并检查 AI 服务日志。
- **可以在服务器使用吗？**
  - 可以，但服务器需安装 Fabric Loader、Fabric API、Carpet Mod 以及本模组，且允许 FakePlayer。
- **HTTP 代理/防火墙导致连接失败？**
  - 使用 `debugMode = true` 查看更详细的连接日志，或改用 `wss://` 并配置证书。

## 🤝 支持与反馈
- GitHub Issues：提交 Bug、功能建议或提问
- Discussions / Discord：即将开放的社区讨论区（敬请期待）

## 📄 许可证
本仓库遵循 **MIT License**。欢迎个人和服务器免费使用，也鼓励二次开发与贡献。

## 📚 更多文档
- 开发者指南与旧版 README 已迁移至 `docs/README-dev.md`
- 架构、API、路线图等文档见 `/docs` 目录下的其它文件

让 MineCompanion-AI 成为你旅途中的最佳伙伴吧！
