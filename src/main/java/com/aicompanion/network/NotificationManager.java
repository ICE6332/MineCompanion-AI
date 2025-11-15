package com.aicompanion.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通知管理器 - 向玩家发送游戏内提示消息
 */
public class NotificationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("NotificationManager");
    private static NotificationManager instance;
    private MinecraftServer server;

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
        server.getPlayerManager().broadcast(message, false);
        LOGGER.info("Connection success notification sent");
    }

    /**
     * 发送连接错误消息
     */
    public void sendConnectionError(String errorReason) {
        if (server == null) return;

        Text message = Text.of("§c✗ Failed to connect to AI Service: " + errorReason);
        server.getPlayerManager().broadcast(message, false);
        LOGGER.info("Connection error notification sent: {}", errorReason);
    }

    /**
     * 发送重连消息
     */
    public void sendReconnecting(int attempt, int maxAttempts) {
        if (server == null) return;

        Text message = Text.of("§e⟳ Reconnecting to AI Service... (attempt " + attempt + "/" + maxAttempts + ")");
        server.getPlayerManager().broadcast(message, false);
        LOGGER.info("Reconnection notification sent: attempt {}/{}", attempt, maxAttempts);
    }

    /**
     * 发送连接断开消息
     */
    public void sendDisconnected(String reason) {
        if (server == null) return;

        Text message = Text.of("§6⊘ Disconnected from AI Service: " + reason);
        server.getPlayerManager().broadcast(message, false);
        LOGGER.info("Disconnection notification sent: {}", reason);
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        server = null;
    }
}
