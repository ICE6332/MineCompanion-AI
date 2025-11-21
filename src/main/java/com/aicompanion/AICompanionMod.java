package com.aicompanion;

import carpet.patches.EntityPlayerMPFake;
import com.aicompanion.command.AICompanionCommand;
import com.aicompanion.config.AICompanionConfig;
import com.aicompanion.listener.ChatInterceptor;
import com.aicompanion.listener.PlayerLifecycleListener;
import com.aicompanion.network.ConnectionManager;
import com.aicompanion.network.NotificationManager;
import com.aicompanion.player.AIFakePlayerManager;
import com.aicompanion.state.GameStateCollector;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MineCompanion-BOT - 主入口点
 *
 * 第二阶段：基于 FakePlayer 的 AI 伙伴与 Carpet Mod 集成
 *
 * 此模组使用 FakePlayer 实体创建智能 AI 伙伴，可以：
 * - 自然地跟随玩家
 * - 看向特定目标
 * - 移动到指定位置
 * - 像真实玩家一样与世界交互
 */
public class AICompanionMod implements ModInitializer {

    /**
     * 模组 ID - 用于日志记录和注册
     */
    public static final String MOD_ID = "aicompanion";

    /**
     * 控制台和日志文件的日志记录器
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * 表示 Carpet Mod 是否已加载的标志
     */
    private static boolean carpetModLoaded = false;

    /**
     * 检查 Carpet Mod 是否已加载并可用
     */
    public static boolean isCarpetModLoaded() {
        return carpetModLoaded;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("========================================");
        LOGGER.info("MineCompanion-BOT 正在初始化...");
        LOGGER.info("版本: 0.4.0-alpha.1 (LLM 对话 + 指令执行)");
        LOGGER.info("========================================");

        // 检查 Carpet Mod 是否已加载
        carpetModLoaded = FabricLoader.getInstance().isModLoaded("carpet");

        if (carpetModLoaded) {
            LOGGER.info("✓ 检测到 Carpet Mod - FakePlayer API 可用");
        } else {
            LOGGER.error("✗ 未找到 Carpet Mod!");
            LOGGER.error("  此模组需要 Carpet Mod 才能工作。");
            LOGGER.error("  请从以下地址安装 Carpet Mod：https://github.com/gnembon/fabric-carpet/releases");
            LOGGER.error("  没有 Carpet Mod，无法创建 AI 伙伴。");
        }

        // 加载配置
        AICompanionConfig.getInstance().load(FabricLoader.getInstance().getConfigDir().toFile());
        LOGGER.info("配置已加载");

        // 注册服务器启动事件
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("服务器已启动 - 正在初始化 AI 系统...");

            // 初始化 WebSocket 连接管理器
            ConnectionManager.getInstance().initialize(server);
            // 注册聊天拦截器（依赖已初始化的连接）
            ChatInterceptor.register();
            // 注册玩家生命周期事件监听，通知后端玩家上下线
            PlayerLifecycleListener.register();

            // 初始化游戏状态收集器
            GameStateCollector.getInstance().initialize(server);

            LOGGER.info("AI 系统已初始化");
        });

        // 注册服务器 tick 事件以更新 AI
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 每个 tick 更新所有 AI 伙伴
            server.getWorlds().forEach(AIFakePlayerManager::tick);
        });
        LOGGER.info("已注册 AI 更新 tick 处理器");

        // 注册世界 tick 事件以进行状态收集
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            // 仅对主世界收集状态以避免重复
            if (world.getRegistryKey() == World.OVERWORLD) {
                GameStateCollector.getInstance().tick();
            }
        });
        LOGGER.info("已注册状态收集 tick 处理器");

        // 注册玩家加入事件以检测 FakePlayer 登录
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // 检查加入的玩家是否是 FakePlayer
            if (handler.player instanceof EntityPlayerMPFake) {
                EntityPlayerMPFake fakePlayer = (EntityPlayerMPFake) handler.player;
                String playerName = fakePlayer.getName().getString();

                LOGGER.info(
                    "FakePlayer '{}' 加入了服务器，尝试注册...",
                    playerName
                );

                // 尝试注册此 FakePlayer（如果它处于待注册状态）
                AIFakePlayerManager.tryRegisterFromJoin(fakePlayer, server);
            }

            // 刷新任何待发送的通知（例如，当没有玩家在线时的连接成功通知）
            NotificationManager.getInstance().flushPendingMessages();

            // 如果此时已经连接上后端，但之前玩家不在线导致未提示，则在玩家加入时补发
            if (ConnectionManager.getInstance().isConnected()) {
                NotificationManager.getInstance().sendConnectionSuccess();
            }
        });
        LOGGER.info("已注册 FakePlayer 加入事件处理器");

        // 注册服务器停止事件以进行清理
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("服务器正在停止 - 清理 AI 系统...");

            // 断开 WebSocket
            ConnectionManager.getInstance().disconnect();

            // 清理通知
            NotificationManager.getInstance().cleanup();

            // 清理 AI 玩家
            AIFakePlayerManager.cleanup();

            LOGGER.info("AI 系统已清理");
        });

        // 注册命令
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> {
                AICompanionCommand.register(dispatcher, registryAccess);
            }
        );
        LOGGER.info("已注册模组命令");

        LOGGER.info("========================================");
        LOGGER.info("MineCompanion-BOT 初始化成功！");
        LOGGER.info("功能:");
        LOGGER.info("  - FakePlayer AI 伙伴");
        LOGGER.info("  - WebSocket 通信: " + (AICompanionConfig.getInstance().isWebSocketEnabled() ? "已启用" : "已禁用"));
        LOGGER.info("  - 状态收集: " + (AICompanionConfig.getInstance().isWebSocketEnabled() ? "已启用" : "已禁用"));
        LOGGER.info("========================================");
    }
}

