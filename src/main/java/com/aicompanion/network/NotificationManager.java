package com.aicompanion.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知管理器 - 向玩家发送游戏内提示消息
 */
public class NotificationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("NotificationManager");
    private static NotificationManager instance;
    private MinecraftServer server;
    private final List<Text> pendingMessages = new ArrayList<>();

    private NotificationManager() {
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * 初始化通知管理器
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
    }

    /**
     * 获取服务器实例
     */
    public MinecraftServer getServer() {
        return server;
    }

    /**
     * 发送连接成功消息
     */
    public void sendConnectionSuccess() {
        if (server == null) return;

        Text message = Text.of("§a✓ AI Service connected successfully!");
        broadcastOrQueue(message);
        LOGGER.info("Connection success notification sent");
    }

    /**
     * 发送连接错误消息
     */
    public void sendConnectionError(String errorReason) {
        if (server == null) return;

        Text message = Text.of("§c✗ Failed to connect to AI Service: " + errorReason);
        broadcastOrQueue(message);
        LOGGER.info("Connection error notification sent: {}", errorReason);
    }

    /**
     * 发送重连消息
     */
    public void sendReconnecting(int attempt, int maxAttempts) {
        if (server == null) return;

        Text message = Text.of("§e⟳ Reconnecting to AI Service... (attempt " + attempt + "/" + maxAttempts + ")");
        broadcastOrQueue(message);
        LOGGER.info("Reconnection notification sent: attempt {}/{}", attempt, maxAttempts);
    }

    /**
     * 发送连接断开消息
     */
    public void sendDisconnected(String reason) {
        if (server == null) return;

        Text message = Text.of("§6⊘ Disconnected from AI Service: " + reason);
        broadcastOrQueue(message);
        LOGGER.info("Disconnection notification sent: {}", reason);
    }

    /**
     * 如果当前没有在线玩家，就暂存消息，待玩家登录时再发送。
     */
    private void broadcastOrQueue(Text message) {
        // server 已存在，但可能还没有玩家（例如世界刚加载）
        if (server.getPlayerManager().getPlayerList().isEmpty()) {
            pendingMessages.add(message);
            LOGGER.debug("No players online, queue notification: {}", message.getString());
            return;
        }
        server.getPlayerManager().broadcast(message, false);
    }

    /**
     * 在玩家加入时调用，发送之前积累的提示消息。
     */
    public void flushPendingMessages() {
        if (server == null) return;
        if (pendingMessages.isEmpty()) return;

        pendingMessages.forEach(msg -> server.getPlayerManager().broadcast(msg, false));
        pendingMessages.clear();
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        server = null;
    }
}
