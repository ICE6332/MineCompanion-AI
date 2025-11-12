package com.aicompanion.client;

import com.aicompanion.AICompanionMod;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initialization for MineCompanion-BOT
 *
 * Phase 2: FakePlayer version - no custom rendering needed
 * FakePlayer entities use the default player renderer automatically
 */
public class AICompanionModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AICompanionMod.LOGGER.info(
            "MineCompanion-BOT client is initializing..."
        );

        // FakePlayer entities don't need custom renderers
        // They use Minecraft's built-in player rendering

        AICompanionMod.LOGGER.info(
            "MineCompanion-BOT client initialized successfully!"
        );
        AICompanionMod.LOGGER.info(
            "FakePlayer entities will use default player rendering"
        );
    }
}

