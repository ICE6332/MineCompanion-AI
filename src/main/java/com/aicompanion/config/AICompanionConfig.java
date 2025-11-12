package com.aicompanion.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * AI Companion Mod 配置管理器
 * 单例模式，负责加载、保存和提供配置参数
 */
public class AICompanionConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("AICompanionConfig");
    private static AICompanionConfig instance;
    private static final String CONFIG_FILE_NAME = "ai-companion.json";

    // WebSocket 配置
    private boolean websocketEnabled = true;
    private String serverUrl = "ws://localhost:8080/ws";
    private boolean autoReconnect = true;
    private int reconnectDelaySeconds = 5;
    private int maxReconnectAttempts = 10;

    // 状态收集配置
    private int stateUpdateIntervalTicks = 20; // 默认每秒更新一次（20 ticks = 1 second）
    private boolean collectEnvironment = true;
    private boolean collectNearbyEntities = false;
    private int entityScanRadius = 16;

    // 调试模式
    private boolean debugMode = false;

    private File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private AICompanionConfig() {
    }

    public static AICompanionConfig getInstance() {
        if (instance == null) {
            instance = new AICompanionConfig();
        }
        return instance;
    }

    /**
     * 加载配置文件
     * @param configDir 配置文件目录
     */
    public void load(File configDir) {
        configFile = new File(configDir, CONFIG_FILE_NAME);

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                parseConfig(json);
                LOGGER.info("AI Companion config loaded from: " + configFile.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.error("Failed to load config, using defaults", e);
                saveDefaults();
            }
        } else {
            LOGGER.info("Config file not found, creating default: " + configFile.getAbsolutePath());
            saveDefaults();
        }
    }

    /**
     * 解析配置 JSON
     */
    private void parseConfig(JsonObject json) {
        if (json.has("websocket")) {
            JsonObject ws = json.getAsJsonObject("websocket");
            websocketEnabled = ws.has("enabled") && ws.get("enabled").getAsBoolean();
            if (ws.has("serverUrl")) serverUrl = ws.get("serverUrl").getAsString();
            if (ws.has("autoReconnect")) autoReconnect = ws.get("autoReconnect").getAsBoolean();
            if (ws.has("reconnectDelaySeconds")) reconnectDelaySeconds = ws.get("reconnectDelaySeconds").getAsInt();
            if (ws.has("maxReconnectAttempts")) maxReconnectAttempts = ws.get("maxReconnectAttempts").getAsInt();
        }

        if (json.has("stateCollection")) {
            JsonObject state = json.getAsJsonObject("stateCollection");
            if (state.has("updateIntervalTicks")) stateUpdateIntervalTicks = state.get("updateIntervalTicks").getAsInt();
            if (state.has("collectEnvironment")) collectEnvironment = state.get("collectEnvironment").getAsBoolean();
            if (state.has("collectNearbyEntities")) collectNearbyEntities = state.get("collectNearbyEntities").getAsBoolean();
            if (state.has("entityScanRadius")) entityScanRadius = state.get("entityScanRadius").getAsInt();
        }

        if (json.has("debugMode")) {
            debugMode = json.get("debugMode").getAsBoolean();
        }
    }

    /**
     * 保存默认配置到文件
     */
    private void saveDefaults() {
        try {
            configFile.getParentFile().mkdirs();

            JsonObject root = new JsonObject();

            JsonObject websocket = new JsonObject();
            websocket.addProperty("enabled", websocketEnabled);
            websocket.addProperty("serverUrl", serverUrl);
            websocket.addProperty("autoReconnect", autoReconnect);
            websocket.addProperty("reconnectDelaySeconds", reconnectDelaySeconds);
            websocket.addProperty("maxReconnectAttempts", maxReconnectAttempts);
            root.add("websocket", websocket);

            JsonObject stateCollection = new JsonObject();
            stateCollection.addProperty("updateIntervalTicks", stateUpdateIntervalTicks);
            stateCollection.addProperty("collectEnvironment", collectEnvironment);
            stateCollection.addProperty("collectNearbyEntities", collectNearbyEntities);
            stateCollection.addProperty("entityScanRadius", entityScanRadius);
            root.add("stateCollection", stateCollection);

            root.addProperty("debugMode", debugMode);

            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(root, writer);
            }

            LOGGER.info("Default config saved to: " + configFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to save default config", e);
        }
    }

    // Getters
    public boolean isWebSocketEnabled() {
        return websocketEnabled;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public int getReconnectDelaySeconds() {
        return reconnectDelaySeconds;
    }

    public int getMaxReconnectAttempts() {
        return maxReconnectAttempts;
    }

    public int getStateUpdateIntervalTicks() {
        return stateUpdateIntervalTicks;
    }

    public boolean isCollectEnvironment() {
        return collectEnvironment;
    }

    public boolean isCollectNearbyEntities() {
        return collectNearbyEntities;
    }

    public int getEntityScanRadius() {
        return entityScanRadius;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    // Setters (for runtime configuration changes)
    public void setWebSocketEnabled(boolean enabled) {
        this.websocketEnabled = enabled;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}
