package com.aicompanion.controller;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

/**
 * Combat controller for AI fake players.
 *
 * Manages a simple combat target and performs basic melee attacks
 * when the target is within range.
 */
public class CombatController {

    private final EntityPlayerMPFake player;
    private Entity target;

    public CombatController(EntityPlayerMPFake player) {
        this.player = player;
    }

    /**
     * Set the current attack target.
     */
    public void setTarget(Entity target) {
        this.target = target;
    }

    /**
     * Clear the current attack target.
     */
    public void clearTarget() {
        this.target = null;
    }

    /**
     * Per-tick combat update.
     *
     * If a target is set and close enough, perform an attack.
     */
    public void tick() {
        if (target == null || target.isRemoved()) {
            return;
        }

        double distanceSq = player.squaredDistanceTo(target);
        if (distanceSq <= 4.0D) {
            // within 2 blocks
            attackOnce();
        }
    }

    /**
     * Perform a single melee attack on the current target.
     */
    public void attackOnce() {
        if (target == null || target.isRemoved()) {
            return;
        }

        player.attack(target);
        player.swingHand(Hand.MAIN_HAND);
    }
}
