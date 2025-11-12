package com.aicompanion.player;

import carpet.patches.EntityPlayerMPFake;
import com.aicompanion.AICompanionMod;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

/**
 * 管理所有 AI FakePlayer 的生命周期
 *
 * 使用 Carpet Mod 的 FakePlayer API 创建和管理 AI 玩家
 */
public class AIFakePlayerManager {

    /**
     * 存储所有活跃的 FakePlayer
     * Key: FakePlayer 的 UUID
     * Value: AIPlayerController 实例
     */
    private static final ConcurrentHashMap<UUID, AIPlayerController> PLAYERS =
        new ConcurrentHashMap<>();

    /**
     * 名称到 UUID 的映射，便于通过名称查找
     * Key: FakePlayer 名称
     * Value: FakePlayer UUID
     */
    private static final ConcurrentHashMap<String, UUID> NAME_TO_UUID =
        new ConcurrentHashMap<>();

    /**
     * 待注册的 FakePlayer（等待登录事件）
     * Key: FakePlayer 名称（忽略大小写）
     * Value: 创建者的 UUID
     */
    private static final ConcurrentHashMap<String, UUID> PENDING_REGISTRATION =
        new ConcurrentHashMap<>();

    /**
     * 创建一个新的 AI FakePlayer
     *
     * @param world 世界实例
     * @param owner 召唤 AI 的玩家
     * @param name AI 的名称
     * @return true 表示成功启动创建，false 表示错误
     */
    public static boolean createAIPlayer(
        ServerWorld world,
        ServerPlayerEntity owner,
        String name
    ) {
        // 0. 首先检查 Carpet Mod 是否加载
        if (!AICompanionMod.isCarpetModLoaded()) {
            owner.sendMessage(
                Text.literal(
                    "§c错误: 无法创建 AI 伙伴！\n" +
                    "§e此功能需要 Carpet Mod 才能使用。\n" +
                    "§7请从以下地址下载并安装 Carpet Mod:\n" +
                    "§bhttps://github.com/gnembon/fabric-carpet/releases"
                ),
                false
            );
            AICompanionMod.LOGGER.error(
                "Attempted to create AI companion '{}' but Carpet Mod is not loaded",
                name
            );
            return false;
        }

        // 1. 检查名称是否已被现有 AI 使用（忽略大小写）
        for (String existingName : NAME_TO_UUID.keySet()) {
            if (existingName.equalsIgnoreCase(name)) {
                owner.sendMessage(
                    Text.literal("§c已存在名为 '" + existingName + "' 的 AI 伙伴！"),
                    false
                );
                return false;
            }
        }

        // 2. 检查是否正在等待注册（忽略大小写）
        for (String pendingName : PENDING_REGISTRATION.keySet()) {
            if (pendingName.equalsIgnoreCase(name)) {
                owner.sendMessage(
                    Text.literal("§eAI 伙伴 '" + pendingName + "' 正在创建中，请稍候..."),
                    false
                );
                return false;
            }
        }

        // 3. 检查是否与在线玩家名称冲突
        for (ServerPlayerEntity player : world
            .getServer()
            .getPlayerManager()
            .getPlayerList()) {
            if (
                player.getName().getString().equalsIgnoreCase(name) &&
                !(player instanceof EntityPlayerMPFake)
            ) {
                owner.sendMessage(
                    Text.literal(
                        "§c无法创建 AI 伙伴 '" +
                        name +
                        "'！该名称与在线玩家冲突。"
                    ),
                    false
                );
                return false;
            }
        }

        // 4. 找到安全的生成位置（玩家旁边）
        Vec3d spawnPos = findSafeSpawnPosition(owner);

        // 5. 使用 Carpet Mod API 创建 FakePlayer
        AICompanionMod.LOGGER.info(
            "Creating FakePlayer '{}' at ({}, {}, {})",
            name,
            spawnPos.x,
            spawnPos.y,
            spawnPos.z
        );

        boolean created;
        try {
            created = EntityPlayerMPFake.createFake(
                name,
                world.getServer(),
                spawnPos,
                (double) owner.getYaw(),
                (double) owner.getPitch(),
                world.getRegistryKey(),
                GameMode.SURVIVAL,
                false
            );
        } catch (Exception e) {
            AICompanionMod.LOGGER.error(
                "Failed to create FakePlayer '{}'",
                name,
                e
            );
            owner.sendMessage(
                Text.literal(
                    "§c创建 AI 伙伴失败！\n" +
                    "§e错误: " + e.getMessage() + "\n" +
                    "§7请查看服务器日志以获取详细信息。"
                ),
                false
            );
            return false;
        }

        // 6. 检查创建是否成功
        if (!created) {
            AICompanionMod.LOGGER.error(
                "EntityPlayerMPFake.createFake() returned false for '{}'",
                name
            );
            owner.sendMessage(
                Text.literal(
                    "§c创建 AI 伙伴失败！\n" +
                    "§eCarpet Mod 返回创建失败。\n" +
                    "§7请检查 Carpet Mod 是否正确配置。"
                ),
                false
            );
            return false;
        }

        // 7. 添加到待注册列表，等待 FakePlayer 登录事件
        // 使用小写作为 key 以支持忽略大小写匹配
        PENDING_REGISTRATION.put(name.toLowerCase(), owner.getUuid());

        AICompanionMod.LOGGER.info(
            "FakePlayer '{}' creation initiated, waiting for join event...",
            name
        );

        owner.sendMessage(
            Text.literal("§e正在创建 AI 伙伴 '" + name + "'，请稍候..."),
            false
        );

        return true;
    }

    /**
     * 移除指定名称的 AI FakePlayer
     *
     * @param name AI 名称
     * @return 成功移除返回 true，否则返回 false
     */
    public static boolean removeAIPlayer(String name) {
        UUID uuid = NAME_TO_UUID.remove(name);
        if (uuid == null) {
            return false;
        }

        AIPlayerController controller = PLAYERS.remove(uuid);
        if (controller != null) {
            controller.cleanup();
            return true;
        }

        return false;
    }

    /**
     * 尝试从登录事件中注册 FakePlayer
     * 当 FakePlayer 登录到服务器时，由事件监听器调用
     *
     * @param fakePlayer 刚刚登录的 FakePlayer
     * @param server Minecraft 服务器实例
     */
    public static void tryRegisterFromJoin(EntityPlayerMPFake fakePlayer, net.minecraft.server.MinecraftServer server) {
        String actualName = fakePlayer.getName().getString();
        String lowerCaseName = actualName.toLowerCase();

        AICompanionMod.LOGGER.info(
            "Attempting to register FakePlayer '{}' from join event",
            actualName
        );

        // 检查是否在待注册列表中（忽略大小写）
        UUID creatorUUID = PENDING_REGISTRATION.remove(lowerCaseName);

        if (creatorUUID == null) {
            AICompanionMod.LOGGER.warn(
                "FakePlayer '{}' joined but was not in pending registration list. " +
                "It may have been created by Carpet Mod directly.",
                actualName
            );
            return;
        }

        // 检查是否已经注册过（防止重复注册）
        if (NAME_TO_UUID.containsKey(actualName)) {
            AICompanionMod.LOGGER.warn(
                "FakePlayer '{}' is already registered, skipping",
                actualName
            );
            return;
        }

        // 创建控制器并注册
        AIPlayerController controller = new AIPlayerController(fakePlayer);
        PLAYERS.put(fakePlayer.getUuid(), controller);
        NAME_TO_UUID.put(actualName, fakePlayer.getUuid());

        AICompanionMod.LOGGER.info(
            "Successfully registered AI companion '{}' (UUID: {})",
            actualName,
            fakePlayer.getUuid()
        );

        // 通知创建者
        ServerPlayerEntity creator = server
            .getPlayerManager()
            .getPlayer(creatorUUID);

        if (creator != null) {
            creator.sendMessage(
                Text.literal("§a成功创建 AI 伙伴 '" + actualName + "'！"),
                false
            );
        }
    }

    /**
     * 通过名称获取 AIPlayerController
     *
     * @param name AI 名称
     * @return AIPlayerController 实例，不存在返回 null
     */
    public static AIPlayerController getPlayerByName(String name) {
        UUID uuid = NAME_TO_UUID.get(name);
        if (uuid == null) {
            return null;
        }
        return PLAYERS.get(uuid);
    }

    /**
     * 通过 UUID 获取 AIPlayerController
     *
     * @param uuid FakePlayer UUID
     * @return AIPlayerController 实例，不存在返回 null
     */
    public static AIPlayerController getPlayerByUUID(UUID uuid) {
        return PLAYERS.get(uuid);
    }

    /**
     * 获取所有活跃的 AI 玩家名称
     *
     * @return AI 名称集合
     */
    public static Collection<String> getAllPlayerNames() {
        return NAME_TO_UUID.keySet();
    }

    /**
     * 获取活跃的 AI 玩家数量
     *
     * @return AI 数量
     */
    public static int getPlayerCount() {
        return PLAYERS.size();
    }

    /**
     * 每个服务器 Tick 调用一次，更新所有 AI
     *
     * @param world 当前世界
     */
    public static void tick(ServerWorld world) {
        // 更新所有活跃的 AI 控制器
        PLAYERS.values().forEach(controller -> {
            try {
                controller.tick();
            } catch (Exception e) {
                // 防止单个 AI 的错误影响其他 AI
                AICompanionMod.LOGGER.error(
                    "Error updating AI companion: {}",
                    e.getMessage(),
                    e
                );
            }
        });
    }

    /**
     * 清理所有 AI FakePlayer（服务器关闭时调用）
     */
    public static void cleanup() {
        PLAYERS.values().forEach(AIPlayerController::cleanup);
        PLAYERS.clear();
        NAME_TO_UUID.clear();
        PENDING_REGISTRATION.clear();
    }

    /**
     * 在玩家附近找到安全的生成位置
     *
     * @param owner 召唤玩家
     * @return 安全的生成位置
     */
    private static Vec3d findSafeSpawnPosition(ServerPlayerEntity owner) {
        // 简单策略：生成在玩家前方 2 格处
        double yaw = Math.toRadians(owner.getYaw());
        double offsetX = -Math.sin(yaw) * 2.0;
        double offsetZ = Math.cos(yaw) * 2.0;

        return new Vec3d(
            owner.getX() + offsetX,
            owner.getY(),
            owner.getZ() + offsetZ
        );
    }
}
