package com.aicompanion.listener;

import carpet.patches.EntityPlayerMPFake;
import com.aicompanion.network.AIWebSocketClient;
import com.aicompanion.network.ConnectionManager;
import com.aicompanion.player.AIFakePlayerManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;

/**
 * ChatInterceptor 拦截玩家聊天消息并转发给后端 LLM 服务。
 * <p>
 * 通过 ALLOW_CHAT_MESSAGE 事件在广播前阻止原始聊天，并把非命令消息包装为
 * conversation_request 标准 JSON 后发送到 WebSocket。
 */
public final class ChatInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger("ChatInterceptor");
    private static final Gson GSON = new Gson();

    private ChatInterceptor() {
    }

    /**
     * 注册聊天拦截事件。
     * 必须在 WebSocket 连接管理器初始化之后调用，以确保发送可用。
     */
    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(ChatInterceptor::onChatMessage);
        LOGGER.info("ChatInterceptor registered.");
    }

    /**
     * 处理玩家聊天消息。
     *
     * @param message 被广播的聊天消息（已应用装饰）
     * @param sender  发送消息的玩家
     * @param params  消息参数
     * @return {@code true} 继续广播；{@code false} 拦截并发送到 AI
     */
    private static boolean onChatMessage(
        SignedMessage message,
        ServerPlayerEntity sender,
        MessageType.Parameters params
    ) {
        // 排除 FakePlayer 的消息，避免 AI 回复被重复拦截
        if (sender instanceof EntityPlayerMPFake) {
            LOGGER.debug("Ignoring message from FakePlayer: {}", sender.getName().getString());
            return true; // 继续广播，不发送到后端
        }

        String content = message.getContent().getString();

        // 命令直接放行
        if (content.startsWith("/")) {
            return true;
        }

        // 发送到 AI Service
        AIWebSocketClient client = ConnectionManager.getInstance().getClient();
        if (client == null || !client.isOpen()) {
            LOGGER.warn("WebSocket not connected, allowing normal chat: {}", content);
            // WebSocket 未连接时，让消息正常广播
            return true;
        }

        // 动态获取当前活跃的 AI 名称（支持单 AI）
        Collection<String> aiNames = AIFakePlayerManager.getAllPlayerNames();
        String companionName;

        if (aiNames.isEmpty()) {
            // 没有活跃的 AI，使用默认名称作为回退
            companionName = "AICompanion";
            LOGGER.debug("No active AI companions found, using default name: {}", companionName);
        } else {
            // 取第一个 AI（当前仅支持单 AI）
            companionName = aiNames.iterator().next();
            LOGGER.debug("Using active AI companion: {}", companionName);
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("type", "conversation_request");
        payload.addProperty("playerName", sender.getName().getString());
        payload.addProperty("message", content);
        payload.addProperty("companionName", companionName); // 动态获取的 AI 名称

        JsonArray position = new JsonArray();
        position.add(sender.getX());
        position.add(sender.getY());
        position.add(sender.getZ());
        payload.add("position", position);

        payload.addProperty("health", sender.getHealth());
        payload.addProperty("timestamp", Instant.now().getEpochSecond());

        String json = GSON.toJson(payload);
        client.send(json);

        LOGGER.debug("Sent chat to AI: {} -> {}", sender.getName().getString(), content);

        // 允许消息继续广播（让玩家看到自己发的消息）
        // AI的回复会通过单独的 conversation_response 消息显示
        return true;
    }
}
