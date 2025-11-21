package com.aicompanion.listener;

import com.aicompanion.network.AIWebSocketClient;
import com.aicompanion.network.ConnectionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 监听玩家连接/断开事件并通知后端服务。
 */
public final class PlayerLifecycleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerLifecycleListener");
    private static final Gson GSON = new Gson();

    private PlayerLifecycleListener() {
    }

    /**
     * 注册玩家生命周期事件监听。
     */
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerDisconnect(handler));
        LOGGER.info("PlayerLifecycleListener registered.");
    }

    private static void onPlayerJoin(ServerPlayNetworkHandler handler) {
        if (handler.player == null) {
            return;
        }

        String playerName = handler.player.getName().getString();
        LOGGER.info("Player '{}' joined the server, notifying backend...", playerName);
        sendLifecycleEvent("player_connected", playerName);
    }

    private static void onPlayerDisconnect(ServerPlayNetworkHandler handler) {
        if (handler.player == null) {
            return;
        }

        String playerName = handler.player.getName().getString();
        LOGGER.info("Player '{}' left the server, notifying backend...", playerName);
        sendLifecycleEvent("player_disconnected", playerName);
    }

    private static void sendLifecycleEvent(String eventType, String playerName) {
        AIWebSocketClient client = ConnectionManager.getInstance().getClient();
        if (client == null || !client.isOpen()) {
            LOGGER.warn(
                "Skip sending {} event for '{}' because WebSocket is not connected.",
                eventType,
                playerName
            );
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("type", eventType);
        payload.addProperty("playerName", playerName);

        try {
            client.send(GSON.toJson(payload));
            LOGGER.debug("Sent lifecycle event {} for player {}", eventType, playerName);
        } catch (Exception e) {
            LOGGER.error("Failed to send lifecycle event {} for player {}", eventType, playerName, e);
        }
    }
}
