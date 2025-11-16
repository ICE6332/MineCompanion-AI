package com.aicompanion.network;

import com.aicompanion.config.AICompanionConfig;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI WebSocket 客户端
 * 负责与 AI Service 的 WebSocket 连接
 */
public class AIWebSocketClient extends WebSocketClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("AIWebSocketClient");

    private final ConnectionManager connectionManager;
    private final MessageHandler messageHandler;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private volatile boolean shouldReconnect = true;

    public AIWebSocketClient(URI serverUri, ConnectionManager connectionManager, MessageHandler messageHandler) {
        super(serverUri);
        this.connectionManager = connectionManager;
        this.messageHandler = messageHandler;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        LOGGER.info("WebSocket connected to: " + getURI());
        reconnectAttempts.set(0);

        // 游戏内提示连接成功（即便后端还会再回 connection_ack，也提前告知玩家）
        NotificationManager.getInstance().sendConnectionSuccess();

        // 发送连接初始化消息
        sendConnectionInit();
    }

    @Override
    public void onMessage(String message) {
        AICompanionConfig config = AICompanionConfig.getInstance();

        if (config.isDebugMode()) {
            LOGGER.debug("Received message: " + message);
        }

        // 交给 MessageHandler 处理
        messageHandler.handleMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.info("WebSocket closed: code={}, reason={}, remote={}", code, reason, remote);

        // 发送游戏内断开连接通知
        NotificationManager.getInstance().sendDisconnected(reason);

        // 如果需要，尝试重连
        if (shouldReconnect && AICompanionConfig.getInstance().isAutoReconnect()) {
            scheduleReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        LOGGER.error("WebSocket error: " + ex.getMessage());

        // 发送游戏内错误通知
        NotificationManager.getInstance().sendConnectionError(ex.getMessage());

        if (AICompanionConfig.getInstance().isDebugMode()) {
            LOGGER.error("WebSocket error details", ex);
        }
    }

    /**
     * 发送连接初始化消息
     */
    private void sendConnectionInit() {
        String initMessage = "{\"type\":\"connection_init\",\"timestamp\":" +
            (System.currentTimeMillis() / 1000) +
            ",\"data\":{\"client\":\"minecraft_mod\",\"version\":\"0.3.2-alpha.3\"}}";
        send(initMessage);
    }

    /**
     * 安排重连
     */
    private void scheduleReconnect() {
        int attempt = reconnectAttempts.incrementAndGet();
        int maxAttempts = AICompanionConfig.getInstance().getMaxReconnectAttempts();

        if (attempt > maxAttempts) {
            LOGGER.warn("Max reconnect attempts ({}) reached. Giving up.", maxAttempts);
            shouldReconnect = false;
            return;
        }

        // 发送游戏内重连通知
        NotificationManager.getInstance().sendReconnecting(attempt, maxAttempts);

        // 指数退避策略
        int delay = Math.min(AICompanionConfig.getInstance().getReconnectDelaySeconds() * attempt, 60);

        LOGGER.info("Scheduling reconnect in {} seconds (attempt {}/{})", delay, attempt, maxAttempts);

        scheduler.schedule(() -> {
            try {
                LOGGER.info("Attempting to reconnect...");
                this.reconnectBlocking();
            } catch (InterruptedException e) {
                LOGGER.error("Reconnect interrupted", e);
                Thread.currentThread().interrupt();
            }
        }, delay, TimeUnit.SECONDS);
    }

    /**
     * 停止自动重连并关闭连接
     */
    public void shutdown() {
        shouldReconnect = false;
        scheduler.shutdown();
        close();
    }
}
