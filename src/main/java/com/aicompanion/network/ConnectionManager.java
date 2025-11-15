package com.aicompanion.network;

import com.aicompanion.config.AICompanionConfig;
import com.aicompanion.network.protocol.Message;
import com.aicompanion.player.AIFakePlayerManager;
import com.aicompanion.state.GameStateData;
import com.google.gson.Gson;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket 连接管理器
 * 单例模式，负责管理与 AI Service 的连接
 */
public class ConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("ConnectionManager");
    private static ConnectionManager instance;

    private AIWebSocketClient client;
    private MessageHandler messageHandler;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Gson gson = new Gson();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    /**
     * 初始化连接管理器
     * @param server Minecraft 服务器实例
     */
    public void initialize(MinecraftServer server) {
        AICompanionConfig config = AICompanionConfig.getInstance();

        // 初始化通知管理器
        NotificationManager.getInstance().initialize(server);

        if (!config.isWebSocketEnabled()) {
            LOGGER.info("WebSocket is disabled in config");
            return;
        }

        // 创建消息处理器
        messageHandler = new MessageHandler();
        messageHandler.setServer(server);

        // 尝试连接
        connect();
    }

    /**
     * 连接到 AI Service
     */
    public void connect() {
        if (client != null && client.isOpen()) {
            LOGGER.warn("WebSocket is already connected");
            return;
        }

        // 确保通知管理器已初始化
        if (NotificationManager.getInstance().getServer() == null) {
            LOGGER.warn("NotificationManager not initialized, skipping connection");
            return;
        }

        AICompanionConfig config = AICompanionConfig.getInstance();
        String url = config.getServerUrl();

        try {
            URI serverUri = new URI(url);
            client = new AIWebSocketClient(serverUri, this, messageHandler);
            client.connect();
            connected.set(true);
            LOGGER.info("Connecting to AI Service at: " + url);
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid WebSocket URL: " + url, e);
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (client != null) {
            client.shutdown();
            client = null;
            connected.set(false);
            LOGGER.info("WebSocket disconnected");
        }
    }

    /**
     * 发送游戏状态更新
     */
    public void sendGameState(GameStateData state) {
        if (client == null || !client.isOpen()) {
            if (AICompanionConfig.getInstance().isDebugMode()) {
                LOGGER.debug("Cannot send game state: not connected");
            }
            return;
        }

        try {
            Message message = new Message("game_state_update", state);
            String json = gson.toJson(message);

            if (AICompanionConfig.getInstance().isDebugMode()) {
                LOGGER.debug("Sending game state: " + json);
            }

            client.send(json);
        } catch (Exception e) {
            LOGGER.error("Failed to send game state", e);
        }
    }

    /**
     * 发送事件通知
     */
    public void sendEvent(String eventType, Object eventData) {
        if (client == null || !client.isOpen()) {
            return;
        }

        try {
            Message message = new Message(eventType, eventData);
            String json = gson.toJson(message);
            client.send(json);
        } catch (Exception e) {
            LOGGER.error("Failed to send event: " + eventType, e);
        }
    }

    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return client != null && client.isOpen();
    }
}
