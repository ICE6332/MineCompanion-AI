package com.aicompanion;

import carpet.patches.EntityPlayerMPFake;
import com.aicompanion.command.AICompanionCommand;
import com.aicompanion.config.AICompanionConfig;
import com.aicompanion.network.ConnectionManager;
import com.aicompanion.player.AIFakePlayerManager;
import com.aicompanion.state.GameStateCollector;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Companion Mod - Main Entry Point
 *
 * Phase 2: FakePlayer-based AI companions with Carpet Mod integration
 *
 * This mod creates intelligent AI companions using FakePlayer entities that can:
 * - Follow players naturally
 * - Look at specific targets
 * - Move to locations
 * - Interact with the world like real players
 */
public class AICompanionMod implements ModInitializer {

    /**
     * The mod ID - used for logging and registration
     */
    public static final String MOD_ID = "aicompanion";

    /**
     * Logger for console and log file output
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Flag indicating whether Carpet Mod is loaded
     */
    private static boolean carpetModLoaded = false;

    /**
     * Check if Carpet Mod is loaded and available
     */
    public static boolean isCarpetModLoaded() {
        return carpetModLoaded;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("========================================");
        LOGGER.info("AI Companion Mod is initializing...");
        LOGGER.info("Version: 0.3.0 (WebSocket + State Collection)");
        LOGGER.info("========================================");

        // Check if Carpet Mod is loaded
        carpetModLoaded = FabricLoader.getInstance().isModLoaded("carpet");

        if (carpetModLoaded) {
            LOGGER.info("✓ Carpet Mod detected - FakePlayer API available");
        } else {
            LOGGER.error("✗ Carpet Mod NOT found!");
            LOGGER.error("  This mod requires Carpet Mod to function.");
            LOGGER.error("  Please install Carpet Mod from: https://github.com/gnembon/fabric-carpet/releases");
            LOGGER.error("  Without Carpet Mod, AI companions cannot be created.");
        }

        // Load configuration
        AICompanionConfig.getInstance().load(FabricLoader.getInstance().getConfigDir().toFile());
        LOGGER.info("Configuration loaded");

        // Register server started event
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("Server started - initializing AI systems...");

            // Initialize WebSocket connection manager
            ConnectionManager.getInstance().initialize(server);

            // Initialize game state collector
            GameStateCollector.getInstance().initialize(server);

            LOGGER.info("AI systems initialized");
        });

        // Register server tick event for AI updates
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Update all AI companions every tick
            server.getWorlds().forEach(AIFakePlayerManager::tick);
        });
        LOGGER.info("Registered AI update tick handler");

        // Register world tick event for state collection
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            // Only collect state for the overworld to avoid duplicates
            if (world.getRegistryKey() == World.OVERWORLD) {
                GameStateCollector.getInstance().tick();
            }
        });
        LOGGER.info("Registered state collection tick handler");

        // Register player join event to detect FakePlayer login
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Check if the joined player is a FakePlayer
            if (handler.player instanceof EntityPlayerMPFake) {
                EntityPlayerMPFake fakePlayer = (EntityPlayerMPFake) handler.player;
                String playerName = fakePlayer.getName().getString();

                LOGGER.info(
                    "FakePlayer '{}' joined the server, attempting registration...",
                    playerName
                );

                // Try to register this FakePlayer if it's pending
                AIFakePlayerManager.tryRegisterFromJoin(fakePlayer, server);
            }
        });
        LOGGER.info("Registered FakePlayer join event handler");

        // Register server stopping event for cleanup
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("Server stopping - cleaning up AI systems...");

            // Disconnect WebSocket
            ConnectionManager.getInstance().disconnect();

            // Cleanup AI players
            AIFakePlayerManager.cleanup();

            LOGGER.info("AI systems cleaned up");
        });

        // Register commands
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> {
                AICompanionCommand.register(dispatcher, registryAccess);
            }
        );
        LOGGER.info("Registered mod commands");

        LOGGER.info("========================================");
        LOGGER.info("AI Companion Mod initialized successfully!");
        LOGGER.info("Features:");
        LOGGER.info("  - FakePlayer AI companions");
        LOGGER.info("  - WebSocket communication: " + (AICompanionConfig.getInstance().isWebSocketEnabled() ? "ENABLED" : "DISABLED"));
        LOGGER.info("  - State collection: " + (AICompanionConfig.getInstance().isWebSocketEnabled() ? "ENABLED" : "DISABLED"));
        LOGGER.info("========================================");
    }
}
