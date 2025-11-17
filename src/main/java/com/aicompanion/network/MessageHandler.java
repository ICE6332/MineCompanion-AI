package com.aicompanion.network;

import com.aicompanion.config.AICompanionConfig;
import com.aicompanion.player.AIFakePlayerManager;
import com.aicompanion.player.AIPlayerController;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息处理器
 * 负责处理从 AI Service 接收到的消息
 */
public class MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("MessageHandler");

    private MinecraftServer server;

    public MessageHandler() {
        // No longer needs AIFakePlayerManager instance - uses static methods
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    /**
     * 处理接收到的 JSON 消息
     */
    public void handleMessage(String jsonMessage) {
        try {
            JsonObject json = JsonParser.parseString(jsonMessage).getAsJsonObject();

            if (!json.has("type")) {
                LOGGER.warn("Message missing 'type' field");
                return;
            }

            String type = json.get("type").getAsString();

            // 必须在主线程执行游戏操作
            if (server != null) {
                server.execute(() -> {
                    try {
                        switch (type) {
                            case "action_command":
                                handleActionCommand(json.getAsJsonObject("data"));
                                break;
                            case "conversation_message":
                                handleConversation(json.getAsJsonObject("data"));
                                break;
                            case "conversation_response":
                                // 支持两种格式：带data包裹的旧版，以及扁平的新版
                                if (json.has("data")) {
                                    handleConversationResponse(json.getAsJsonObject("data"));
                                } else {
                                    handleConversationResponse(json);
                                }
                                break;
                            case "config_sync":
                                handleConfigSync(json.getAsJsonObject("data"));
                                break;
                            case "connection_ack":
                            case "connection_accept":
                                LOGGER.info("Connection accepted by AI Service");
                                NotificationManager.getInstance().sendConnectionSuccess();
                                break;
                            case "game_state_ack":
                                LOGGER.debug("Game state acknowledged by AI Service");
                                break;
                            case "error_notification":
                                handleError(json.getAsJsonObject("data"));
                                break;
                            default:
                                LOGGER.warn("Unknown message type: " + type);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error handling message of type: " + type, e);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse message: " + jsonMessage, e);
        }
    }

    /**
     * 处理动作指令
     */
    private void handleActionCommand(JsonObject data) {
        if (!data.has("companionName") || !data.has("action")) {
            LOGGER.warn("action_command missing required fields");
            return;
        }

        String companionName = data.get("companionName").getAsString();
        String action = data.get("action").getAsString();

        AIPlayerController controller = AIFakePlayerManager.getPlayerByName(companionName);
        if (controller == null) {
            LOGGER.warn("AI companion not found: " + companionName);
            return;
        }

        if (AICompanionConfig.getInstance().isDebugMode()) {
            LOGGER.debug("Executing action: {} for {}", action, companionName);
        }

        switch (action) {
            case "follow":
                if (data.has("target")) {
                    String targetName = data.get("target").getAsString();
                    ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);
                    if (target != null) {
                        controller.getMovementController().setFollowTarget(target);
                        LOGGER.info("{} now following {}", companionName, targetName);
                    }
                }
                break;

            case "look":
                if (data.has("target")) {
                    String targetName = data.get("target").getAsString();
                    ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);
                    if (target != null) {
                        controller.getViewController().setLookTarget(target);
                        LOGGER.info("{} now looking at {}", companionName, targetName);
                    }
                }
                break;

            case "stop":
                controller.getMovementController().stop();
                controller.getViewController().stopLooking();
                LOGGER.info("{} stopped all behaviors", companionName);
                break;

            case "move_to":
                if (data.has("position")) {
                    JsonObject posObj = data.getAsJsonObject("position");
                    double x = posObj.get("x").getAsDouble();
                    double y = posObj.get("y").getAsDouble();
                    double z = posObj.get("z").getAsDouble();

                    controller
                        .getMovementController()
                        .moveTo(new net.minecraft.util.math.Vec3d(x, y, z));
                    LOGGER.info(
                        "{} moving to position ({}, {}, {})",
                        companionName,
                        x,
                        y,
                        z
                    );
                } else {
                    LOGGER.warn("move_to action missing 'position' field");
                }
                break;

            case "jump":
                controller.getMovementController().jump();
                LOGGER.info("{} jumping", companionName);
                break;

            case "mine_block":
                if (data.has("position")) {
                    JsonObject posObj = data.getAsJsonObject("position");
                    int x = posObj.get("x").getAsInt();
                    int y = posObj.get("y").getAsInt();
                    int z = posObj.get("z").getAsInt();
                    BlockPos pos = new BlockPos(x, y, z);

                    controller.getInteractionController().mineBlock(pos);
                    LOGGER.info(
                        "{} mining block at ({}, {}, {})",
                        companionName,
                        x,
                        y,
                        z
                    );
                } else {
                    LOGGER.warn("mine_block action missing 'position' field");
                }
                break;

            case "place_block":
                if (data.has("position")) {
                    JsonObject posObj = data.getAsJsonObject("position");
                    int x = posObj.get("x").getAsInt();
                    int y = posObj.get("y").getAsInt();
                    int z = posObj.get("z").getAsInt();
                    BlockPos pos = new BlockPos(x, y, z);

                    controller.getInteractionController().placeBlock(pos);
                    LOGGER.info(
                        "{} placing block at ({}, {}, {})",
                        companionName,
                        x,
                        y,
                        z
                    );
                } else {
                    LOGGER.warn("place_block action missing 'position' field");
                }
                break;

            case "use_item":
                // 骨架版本：默认在空中使用当前物品
                controller.getInteractionController().useItemInAir();
                LOGGER.info("{} using item in air", companionName);
                break;

            case "attack_entity":
                // 骨架版本：通过玩家名作为攻击目标
                if (data.has("targetPlayer")) {
                    String targetName = data.get("targetPlayer").getAsString();
                    ServerPlayerEntity target =
                        server.getPlayerManager().getPlayer(targetName);
                    if (target != null) {
                        controller.getCombatController().setTarget(target);
                        LOGGER.info(
                            "{} targeting player {} for attack",
                            companionName,
                            targetName
                        );
                    } else {
                        LOGGER.warn("attack_entity target player not found: {}", targetName);
                    }
                } else {
                    LOGGER.warn("attack_entity action missing 'targetPlayer' field");
                }
                break;

            default:
                LOGGER.warn("Unknown action: " + action);
        }
    }

    /**
     * 处理对话响应（新协议）
     * 优先兼容文档中的 conversation_response 结构：
     * {
     *   "type": "conversation_response",
     *   "data": {
     *     "ai": "AICompanion",
     *     "message": "内容"
     *   }
     * }
     * 同时兼容旧的 conversation_message 结构：
     * {
     *   "companionName": "...",
     *   "text": "..."
     * }
     */
    private void handleConversationResponse(JsonObject data) {
        JsonObject normalized = new JsonObject();
        // 兼容 action / a 字段的动作列表
        if (data.has("action") && data.get("action").isJsonArray()) {
            processActions(data.getAsJsonArray("action"));
        } else if (data.has("a") && data.get("a").isJsonArray()) {
            processActions(data.getAsJsonArray("a"));
        }

        // 紧凑格式：c（companion），m（message）
        if (data.has("c") && data.has("m")) {
            normalized.addProperty("companionName", data.get("c").getAsString());
            normalized.addProperty("text", data.get("m").getAsString());
            LOGGER.debug("Parsed compact conversation_response");
        } else if (data.has("companionName") && data.has("message")) {
            // 新版标准格式（扁平结构）
            normalized.addProperty("companionName", data.get("companionName").getAsString());
            normalized.addProperty("text", data.get("message").getAsString());
            LOGGER.debug("Parsed standard conversation_response (new format)");
        } else if (data.has("ai") && data.has("message")) {
            // 旧版标准格式（向后兼容）
            normalized.addProperty("companionName", data.get("ai").getAsString());
            normalized.addProperty("text", data.get("message").getAsString());
            LOGGER.debug("Parsed standard conversation_response (old format)");
        } else if (data.has("companionName") && data.has("text")) {
            // 已经是标准格式
            normalized = data;
        } else {
            LOGGER.warn("conversation_response missing required fields, received: {}", data.toString());
            return;
        }

        handleConversation(normalized);
    }

    /**
     * 处理 AI 返回的动作列表。
     *
     * @param actions JSON 数组，包含 action 或 a 字段的动作对象
     */
    private void processActions(com.google.gson.JsonArray actions) {
        if (actions == null || actions.isEmpty()) {
            return;
        }

        for (int i = 0; i < actions.size(); i++) {
            if (!actions.get(i).isJsonObject()) {
                LOGGER.warn("Action at index {} is not an object", i);
                continue;
            }

            JsonObject actionObj = actions.get(i).getAsJsonObject();
            String type = null;

            if (actionObj.has("type")) {
                type = actionObj.get("type").getAsString();
            } else if (actionObj.has("t")) {
                type = actionObj.get("t").getAsString();
            }

            if (type == null) {
                LOGGER.warn("Action at index {} missing type field", i);
                continue;
            }

            switch (type) {
                case "command":
                    String command = null;
                    if (actionObj.has("command")) {
                        command = actionObj.get("command").getAsString();
                    } else if (actionObj.has("c")) {
                        command = actionObj.get("c").getAsString();
                    }

                    if (command == null || command.isBlank()) {
                        LOGGER.warn("Command action at index {} missing 'command' text", i);
                        continue;
                    }

                    executeServerCommand(command.trim());
                    break;
                default:
                    LOGGER.warn("Unsupported action type '{}' at index {}", type, i);
            }
        }
    }

    /**
     * 执行经过白名单的服务器命令。
     *
     * @param rawCommand 完整命令字符串（可含前导 /）
     */
    private void executeServerCommand(String rawCommand) {
        if (server == null) {
            LOGGER.warn("Server is null, cannot execute command: {}", rawCommand);
            return;
        }

        String command = rawCommand.trim();
        if (!command.startsWith("/")) {
            command = "/" + command;
        }

        String[] parts = command.split("\\s+");
        if (parts.length == 0) {
            LOGGER.warn("Empty command received from AI actions");
            return;
        }

        String base = parts[0].toLowerCase(java.util.Locale.ROOT);

        try {
            switch (base) {
                case "/follow":
                    if (parts.length < 3) {
                        LOGGER.warn("Follow command missing arguments: {}", command);
                        return;
                    }
                    handleFollowCommand(parts[1], parts[2]);
                    break;
                case "/stop":
                    if (parts.length < 2) {
                        LOGGER.warn("Stop command missing companion name: {}", command);
                        return;
                    }
                    handleStopCommand(parts[1]);
                    break;
                case "/say":
                    handleSayCommand(command.substring(4).trim());
                    break;
                case "/look":
                    if (parts.length < 3) {
                        LOGGER.warn("Look command missing arguments: {}", command);
                        return;
                    }
                    handleLookCommand(parts[1], parts[2]);
                    break;
                default:
                    LOGGER.warn("Rejected non-whitelisted command from AI: {}", command);
            }
        } catch (Exception e) {
            LOGGER.error("Error executing AI command: {}", command, e);
        }
    }

    private void handleFollowCommand(String companionName, String targetName) {
        AIPlayerController controller = AIFakePlayerManager.getPlayerByName(companionName);
        if (controller == null) {
            LOGGER.warn("Companion not found for follow: {}", companionName);
            return;
        }

        ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);
        if (target == null) {
            LOGGER.warn("Follow target player not found: {}", targetName);
            return;
        }

        controller.getMovementController().setFollowTarget(target);
        LOGGER.info("{} now following {}", companionName, targetName);
    }

    private void handleStopCommand(String companionName) {
        AIPlayerController controller = AIFakePlayerManager.getPlayerByName(companionName);
        if (controller == null) {
            LOGGER.warn("Companion not found for stop: {}", companionName);
            return;
        }

        controller.getMovementController().stop();
        controller.getMovementController().setFollowTarget(null);
        controller.getViewController().stopLooking();
        LOGGER.info("{} stopped all behaviors", companionName);
    }

    private void handleSayCommand(String message) {
        if (message.isBlank()) {
            LOGGER.warn("Say command missing message content");
            return;
        }
        server.getPlayerManager().broadcast(Text.literal(message), false);
        LOGGER.info("Broadcasted AI message: {}", message);
    }

    private void handleLookCommand(String companionName, String targetName) {
        AIPlayerController controller = AIFakePlayerManager.getPlayerByName(companionName);
        if (controller == null) {
            LOGGER.warn("Companion not found for look: {}", companionName);
            return;
        }

        ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);
        if (target == null) {
            LOGGER.warn("Look target player not found: {}", targetName);
            return;
        }

        controller.getViewController().setLookTarget(target);
        LOGGER.info("{} now looking at {}", companionName, targetName);
    }

    /**
     * 处理对话消息
     */
    private void handleConversation(JsonObject data) {
        if (!data.has("companionName") || !data.has("text")) {
            LOGGER.warn("conversation_message missing required fields");
            return;
        }

        String companionName = data.get("companionName").getAsString();
        String text = data.get("text").getAsString();

        // 即便对应的 AI 未注册，也允许发送聊天，便于联调
        if (server == null) {
            LOGGER.warn("Server is null, cannot broadcast conversation.");
            return;
        }

        server.getPlayerManager().broadcast(
            Text.literal("<" + companionName + "> " + text),
            false
        );

        LOGGER.info("{} says: {}", companionName, text);
    }

    /**
     * 处理配置同步
     */
    private void handleConfigSync(JsonObject data) {
        LOGGER.info("Config sync received: " + data.toString());
        // 未来扩展：运行时配置更新
    }

    /**
     * 处理错误通知
     */
    private void handleError(JsonObject data) {
        if (data.has("message")) {
            LOGGER.error("AI Service error: " + data.get("message").getAsString());
        }
    }
}
