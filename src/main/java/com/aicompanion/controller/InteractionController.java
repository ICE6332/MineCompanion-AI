package com.aicompanion.controller;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Interaction controller for AI fake players.
 *
 * Handles simple interactions with the world, such as breaking and placing blocks
 * and using items.
 */
public class InteractionController {

    private final EntityPlayerMPFake player;

    public InteractionController(EntityPlayerMPFake player) {
        this.player = player;
    }

    /**
     * Break a block at the given position.
     * Simple implementation: instantly breaks the block and drops items.
     */
    public void mineBlock(BlockPos pos) {
        if (!(player.getEntityWorld() instanceof ServerWorld world)) {
            return;
        }

        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return;
        }

        world.breakBlock(pos, true, player);
        player.swingHand(Hand.MAIN_HAND);
    }

    /**
     * Place a block at the given position using the item in the main hand.
     * Only works when the main hand item is a BlockItem and the target position is air.
     */
    public void placeBlock(BlockPos pos) {
        if (!(player.getEntityWorld() instanceof ServerWorld world)) {
            return;
        }

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        BlockState current = world.getBlockState(pos);
        if (!current.isAir()) {
            return;
        }

        Block block = blockItem.getBlock();
        BlockState newState = block.getDefaultState();
        world.setBlockState(pos, newState);

        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        player.swingHand(Hand.MAIN_HAND);
    }

    /**
     * Use the main hand item on a block at the given position.
     */
    public void useItemOnBlock(BlockPos pos) {
        if (!(player.getEntityWorld() instanceof ServerWorld world)) {
            return;
        }

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            return;
        }

        Vec3d hitPos = Vec3d.ofCenter(pos);
        BlockHitResult hitResult = new BlockHitResult(hitPos, Direction.UP, pos, false);
        ItemUsageContext context = new ItemUsageContext(player, Hand.MAIN_HAND, hitResult);

        stack.useOnBlock(context);
        player.swingHand(Hand.MAIN_HAND);
    }

    /**
     * Use the main hand item in the air (for example eating or drinking).
     */
    public void useItemInAir() {
        if (!(player.getEntityWorld() instanceof ServerWorld world)) {
            return;
        }

        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            return;
        }

        stack.use(world, player, Hand.MAIN_HAND);
        player.swingHand(Hand.MAIN_HAND);
    }

    /**
     * Tick hook for future interaction logic (currently unused).
     */
    public void tick() {
        // No per-tick interaction logic yet.
    }
}

