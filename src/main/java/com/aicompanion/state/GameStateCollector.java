package com.aicompanion.state;

import com.aicompanion.config.AICompanionConfig;
import com.aicompanion.network.ConnectionManager;
import com.aicompanion.player.AIFakePlayerManager;
import com.aicompanion.player.AIPlayerController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 游戏状态收集器
 * 单例模式，负责收集游戏状态并发送给 AI Service
 */
public class GameStateCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger("GameStateCollector");
    private static GameStateCollector instance;

    private MinecraftServer server;
    private int tickCounter = 0;

    private GameStateCollector() {
    }

    public static GameStateCollector getInstance() {
        if (instance == null) {
            instance = new GameStateCollector();
        }
        return instance;
    }

    /**
     * 初始化收集器
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        LOGGER.info("GameStateCollector initialized");
    }

    /**
     * 每个游戏 Tick 调用一次
     */
    public void tick() {
        if (server == null || !AICompanionConfig.getInstance().isWebSocketEnabled()) {
            return;
        }

        tickCounter++;

        int interval = AICompanionConfig.getInstance().getStateUpdateIntervalTicks();
        if (tickCounter >= interval) {
            tickCounter = 0;
            collectAndSend();
        }
    }

    /**
     * 收集状态并发送到 AI Service
     */
    private void collectAndSend() {
        if (!ConnectionManager.getInstance().isConnected()) {
            return;
        }

        try {
            // 为每个真实玩家收集状态（有 AI 伙伴跟随的玩家）
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // 检查该玩家是否有 AI 伙伴
                if (hasCompanions(player)) {
                    GameStateData state = collectStateForPlayer(player);
                    ConnectionManager.getInstance().sendGameState(state);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error collecting game state", e);
        }
    }

    /**
     * 检查玩家是否有 AI 伙伴
     */
    private boolean hasCompanions(ServerPlayerEntity player) {
        // 遍历所有 AI 玩家，检查是否有跟随该玩家的
        for (String aiName : AIFakePlayerManager.getAllPlayerNames()) {
            AIPlayerController controller = AIFakePlayerManager.getPlayerByName(aiName);
            if (controller != null &&
                controller.getMovementController().isFollowing() &&
                controller.getMovementController().getFollowTarget() == player) {
                return true;
            }
        }
        return false;
    }

    /**
     * 收集指定玩家的完整游戏状态
     */
    private GameStateData collectStateForPlayer(ServerPlayerEntity player) {
        GameStateData state = new GameStateData();

        state.player = collectPlayerState(player);
        state.companions = collectCompanionStates(player);
        // In Minecraft 1.21.10, use getEntityWorld() method
        state.environment = collectEnvironmentState((ServerWorld) player.getEntityWorld());

        return state;
    }

    /**
     * 收集玩家状态
     */
    private PlayerStateData collectPlayerState(ServerPlayerEntity player) {
        PlayerStateData data = new PlayerStateData();

        data.username = player.getName().getString();
        data.health = player.getHealth();
        data.hunger = player.getHungerManager().getFoodLevel();

        data.position = new Position(
            player.getX(),
            player.getY(),
            player.getZ()
        );

        // 获取维度 - use getEntityWorld() method in 1.21.10
        Identifier dimensionId = ((ServerWorld) player.getEntityWorld()).getRegistryKey().getValue();
        data.dimension = dimensionId.toString();

        // 获取游戏模式
        data.gameMode = player.interactionManager.getGameMode().name().toLowerCase();

        // 获取玩家正在看的内容（简化版）
        data.lookingAt = "unknown";

        return data;
    }

    /**
     * 收集 AI 伙伴状态
     */
    private List<AIStateData> collectCompanionStates(ServerPlayerEntity player) {
        List<AIStateData> companions = new ArrayList<>();

        // 遍历所有 AI 玩家
        for (String aiName : AIFakePlayerManager.getAllPlayerNames()) {
            AIPlayerController controller = AIFakePlayerManager.getPlayerByName(aiName);

            if (controller == null) {
                continue;
            }

            // 只收集跟随该玩家的 AI
            if (controller.getMovementController().isFollowing() &&
                controller.getMovementController().getFollowTarget() == player) {

                PlayerEntity aiPlayer = controller.getFakePlayer();
                if (aiPlayer != null) {
                    AIStateData data = new AIStateData();
                    data.name = aiName;
                    data.position = new Position(
                        aiPlayer.getX(),
                        aiPlayer.getY(),
                        aiPlayer.getZ()
                    );
                    data.health = aiPlayer.getHealth();
                    data.currentAction = determineCurrentAction(controller);

                    companions.add(data);
                }
            }
        }

        return companions;
    }

    /**
     * 判断 AI 当前的动作
     */
    private String determineCurrentAction(AIPlayerController controller) {
        if (controller.getMovementController().isFollowing()) {
            return "following";
        } else if (controller.getViewController().isLooking()) {
            return "looking";
        } else {
            return "idle";
        }
    }

    /**
     * 收集环境状态
     */
    private EnvironmentStateData collectEnvironmentState(ServerWorld world) {
        if (!AICompanionConfig.getInstance().isCollectEnvironment()) {
            return new EnvironmentStateData("unknown", "unknown", "unknown");
        }

        EnvironmentStateData data = new EnvironmentStateData();

        // 时间
        long timeOfDay = world.getTimeOfDay() % 24000;
        if (timeOfDay < 12000) {
            data.time = "day";
        } else if (timeOfDay < 13000) {
            data.time = "sunset";
        } else if (timeOfDay < 23000) {
            data.time = "night";
        } else {
            data.time = "sunrise";
        }

        // 天气
        if (world.isThundering()) {
            data.weather = "thunder";
        } else if (world.isRaining()) {
            data.weather = "rain";
        } else {
            data.weather = "clear";
        }

        // 生物群系（简化版）
        data.biome = "unknown";

        return data;
    }
}
