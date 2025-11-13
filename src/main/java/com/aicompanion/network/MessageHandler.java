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
                            case "config_sync":
                                handleConfigSync(json.getAsJsonObject("data"));
                                break;
                            case "connection_accept":
                                LOGGER.info("Connection accepted by AI Service");
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

            default:
                LOGGER.warn("Unknown action: " + action);
        }
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

        AIPlayerController controller = AIFakePlayerManager.getPlayerByName(companionName);
        if (controller == null) {
            LOGGER.warn("AI companion not found: " + companionName);
            return;
        }

        // 让 AI 在聊天中发送消息
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
