package com.aicompanion.command;

import carpet.patches.EntityPlayerMPFake;
import com.aicompanion.player.AIFakePlayerManager;
import com.aicompanion.player.AIPlayerController;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * AI 伙伴指令系统 - FakePlayer 版本
 *
 * 提供生成、控制、管理 AI FakePlayer 的命令
 */
public class AICompanionCommand {

    /**
     * 注册所有指令
     */
    public static void register(
        CommandDispatcher<ServerCommandSource> dispatcher,
        CommandRegistryAccess registryAccess
    ) {
        dispatcher.register(
            CommandManager.literal("aicompanion")
                .requires(source -> source.hasPermissionLevel(2)) // 需要管理员权限
                // /aicompanion spawn <名字>
                .then(
                    CommandManager.literal("spawn").then(
                        CommandManager.argument(
                            "name",
                            StringArgumentType.string()
                        ).executes(AICompanionCommand::spawnCompanion)
                    )
                )
                // /aicompanion kill <名字>
                .then(
                    CommandManager.literal("kill").then(
                        CommandManager.argument(
                            "name",
                            StringArgumentType.string()
                        ).executes(AICompanionCommand::killCompanion)
                    )
                )
                // /aicompanion list
                .then(
                    CommandManager.literal("list").executes(
                        AICompanionCommand::listCompanions
                    )
                )
                // /aicompanion follow <名字> [玩家名]
                .then(
                    CommandManager.literal("follow").then(
                        CommandManager.argument(
                            "name",
                            StringArgumentType.string()
                        )
                            .executes(AICompanionCommand::followSelf)
                            .then(
                                CommandManager.argument(
                                    "player",
                                    StringArgumentType.word()
                                ).executes(AICompanionCommand::followPlayer)
                            )
                    )
                )
                // /aicompanion look <名字> [玩家名]
                .then(
                    CommandManager.literal("look").then(
                        CommandManager.argument(
                            "name",
                            StringArgumentType.string()
                        )
                            .executes(AICompanionCommand::lookAtSelf)
                            .then(
                                CommandManager.argument(
                                    "player",
                                    StringArgumentType.word()
                                ).executes(AICompanionCommand::lookAtPlayer)
                            )
                    )
                )
                // /aicompanion stop <名字>
                .then(
                    CommandManager.literal("stop").then(
                        CommandManager.argument(
                            "name",
                            StringArgumentType.string()
                        ).executes(AICompanionCommand::stopBehaviors)
                    )
                )
        );
    }

    /**
     * 生成 AI FakePlayer（同步）
     */
    private static int spawnCompanion(
        CommandContext<ServerCommandSource> context
    ) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String name = StringArgumentType.getString(context, "name");

        // 获取玩家和世界
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerWorld world = source.getWorld();

        // 创建 FakePlayer（同步）
        boolean success = AIFakePlayerManager.createAIPlayer(
            world,
            player,
            name
        );

        // createAIPlayer() 已经发送了成功或失败的消息
        return success ? 1 : 0;
    }

    /**
     * 移除 AI FakePlayer
     */
    private static int killCompanion(
        CommandContext<ServerCommandSource> context
    ) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String name = StringArgumentType.getString(context, "name");

        // 移除 FakePlayer
        boolean removed = AIFakePlayerManager.removeAIPlayer(name);

        if (!removed) {
            source.sendError(Text.literal("找不到 AI 伙伴 '" + name + "'！"));
            return 0;
        }

        source.sendFeedback(
            () -> Text.literal("§c已移除 AI 伙伴: §e" + name),
            true
        );
        return 1;
    }

    /**
     * 列出所有 AI FakePlayer
     */
    private static int listCompanions(
        CommandContext<ServerCommandSource> context
    ) {
        ServerCommandSource source = context.getSource();

        int count = AIFakePlayerManager.getPlayerCount();

        if (count == 0) {
            source.sendFeedback(
                () -> Text.literal("§7当前没有 AI 伙伴"),
                false
            );
            return 0;
        }

        source.sendFeedback(
            () -> Text.literal("§a当前 AI 伙伴列表 (共 " + count + " 个):"),
            false
        );

        AIFakePlayerManager.getAllPlayerNames().forEach(name -> {
            AIPlayerController controller =
                AIFakePlayerManager.getPlayerByName(name);
            if (controller != null) {
                EntityPlayerMPFake fakePlayer = controller.getFakePlayer();
                source.sendFeedback(
                    () ->
                        Text.literal(
                            "§e" +
                                name +
                                " §7(" +
                                String.format(
                                    "%.1f, %.1f, %.1f",
                                    fakePlayer.getX(),
                                    fakePlayer.getY(),
                                    fakePlayer.getZ()
                                ) +
                                ")"
                        ),
                    false
                );
            }
        });

        return count;
    }

    /**
     * 让 AI 伙伴跟随自己
     */
    private static int followSelf(CommandContext<ServerCommandSource> context)
        throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String name = StringArgumentType.getString(context, "name");
        ServerPlayerEntity player = source.getPlayerOrThrow();

        return followPlayerInternal(source, name, player);
    }

    /**
     * 让 AI 伙伴跟随指定玩家
     */
    private static int followPlayer(CommandContext<ServerCommandSource> context)
        throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String name = StringArgumentType.getString(context, "name");
        String playerName = StringArgumentType.getString(context, "player");

        // 查找目标玩家
        ServerPlayerEntity targetPlayer = source
            .getServer()
            .getPlayerManager()
            .getPlayer(playerName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("找不到玩家 '" + playerName + "'！"));
            return 0;
        }

        return followPlayerInternal(source, name, targetPlayer);
    }

    /**
     * 内部方法：设置跟随
     */
    private static int followPlayerInternal(
        ServerCommandSource source,
        String name,
        ServerPlayerEntity targetPlayer
    ) {
        // 查找 AI 伙伴
        AIPlayerController controller = AIFakePlayerManager.getPlayerByName(
            name
        );
        if (controller == null) {
            source.sendError(Text.literal("找不到 AI 伙伴 '" + name + "'！"));
            return 0;
        }

        // 设置跟随
        controller.getMovementController().setFollowTarget(targetPlayer);

        source.sendFeedback(
            () ->
                Text.literal(
                    "§a" + name + " 开始跟随 §e" + targetPlayer.getName().getString()
                ),
            true
        );
        return 1;
    }

    /**
     * 让 AI 伙伴看向自己
     */
    private static int lookAtSelf(CommandContext<ServerCommandSource> context)
        throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String name = StringArgumentType.getString(context, "name");
        ServerPlayerEntity player = source.getPlayerOrThrow();

        return lookAtPlayerInternal(source, name, player);
    }

    /**
     * 让 AI 伙伴看向指定玩家
     */
    private static int lookAtPlayer(CommandContext<ServerCommandSource> context)
        throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String name = StringArgumentType.getString(context, "name");
        String playerName = StringArgumentType.getString(context, "player");

        // 查找目标玩家
        ServerPlayerEntity targetPlayer = source
            .getServer()
            .getPlayerManager()
            .getPlayer(playerName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("找不到玩家 '" + playerName + "'！"));
            return 0;
        }

        return lookAtPlayerInternal(source, name, targetPlayer);
    }

    /**
     * 内部方法：设置看向
     */
    private static int lookAtPlayerInternal(
        ServerCommandSource source,
        String name,
        ServerPlayerEntity targetPlayer
    ) {
        // 查找 AI 伙伴
        AIPlayerController controller = AIFakePlayerManager.getPlayerByName(
            name
        );
        if (controller == null) {
            source.sendError(Text.literal("找不到 AI 伙伴 '" + name + "'！"));
            return 0;
        }

        // 设置看向
        controller.getViewController().setLookTarget(targetPlayer);

        source.sendFeedback(
            () ->
                Text.literal(
                    "§a" + name + " 开始看向 §e" + targetPlayer.getName().getString()
                ),
            true
        );
        return 1;
    }

    /**
     * 停止 AI 伙伴的所有行为
     */
    private static int stopBehaviors(
        CommandContext<ServerCommandSource> context
    ) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String name = StringArgumentType.getString(context, "name");

        // 查找 AI 伙伴
        AIPlayerController controller = AIFakePlayerManager.getPlayerByName(
            name
        );
        if (controller == null) {
            source.sendError(Text.literal("找不到 AI 伙伴 '" + name + "'！"));
            return 0;
        }

        // 停止所有行为
        controller.getMovementController().stop();
        controller.getMovementController().setFollowTarget(null);
        controller.getViewController().stopLooking();

        source.sendFeedback(
            () -> Text.literal("§a" + name + " 已停止所有行为"),
            true
        );
        return 1;
    }
}
