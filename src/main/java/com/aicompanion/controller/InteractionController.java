package com.aicompanion.controller;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * 交互控制器
 *
 * 负责处理 FakePlayer 与世界的交互行为，例如破坏方块。
 */
public class InteractionController {

    private final EntityPlayerMPFake player;

    public InteractionController(EntityPlayerMPFake player) {
        this.player = player;
    }

    /**
     * 破坏指定位置的方块（MVP：瞬间破坏，无挖掘时间模拟）。
     *
     * @param pos 方块位置
     */
    public void mineBlock(BlockPos pos) {
        if (player.getEntityWorld() instanceof ServerWorld world) {
            BlockState state = world.getBlockState(pos);

            if (!state.isAir()) {
                world.breakBlock(pos, true, player);
                player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    /**
     * 每 tick 更新，用于将来扩展挖掘进度等逻辑。
     */
    public void tick() {
        // MVP 阶段暂不需要每 tick 逻辑
    }
}

