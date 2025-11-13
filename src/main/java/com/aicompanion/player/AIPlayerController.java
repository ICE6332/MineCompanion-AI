package com.aicompanion.player;

import carpet.patches.EntityPlayerMPFake;
import com.aicompanion.controller.CombatController;
import com.aicompanion.controller.InteractionController;
import com.aicompanion.controller.InventoryController;
import com.aicompanion.controller.MovementController;
import com.aicompanion.controller.ViewController;

/**
 * AI 玩家控制器
 *
 * 封装 FakePlayer 并提供控制接口。
 */
public class AIPlayerController {

    private final EntityPlayerMPFake fakePlayer;
    private final MovementController movementController;
    private final ViewController viewController;
    private final InteractionController interactionController;
    private final InventoryController inventoryController;
    private final CombatController combatController;

    /**
     * 构造函数
     *
     * @param fakePlayer Carpet Mod 的 FakePlayer 实例
     */
    public AIPlayerController(EntityPlayerMPFake fakePlayer) {
        this.fakePlayer = fakePlayer;
        this.movementController = new MovementController(fakePlayer);
        this.viewController = new ViewController(fakePlayer);
        this.interactionController = new InteractionController(fakePlayer);
        this.inventoryController = new InventoryController(fakePlayer);
        this.combatController = new CombatController(fakePlayer);
    }

    /**
     * 获取底层 FakePlayer 实例。
     */
    public EntityPlayerMPFake getFakePlayer() {
        return fakePlayer;
    }

    /**
     * 获取移动控制器。
     */
    public MovementController getMovementController() {
        return movementController;
    }

    /**
     * 获取视角控制器。
     */
    public ViewController getViewController() {
        return viewController;
    }

    /**
     * 获取交互控制器。
     */
    public InteractionController getInteractionController() {
        return interactionController;
    }

    /**
     * 获取背包控制器。
     */
    public InventoryController getInventoryController() {
        return inventoryController;
    }

    /**
     * 获取战斗控制器。
     */
    public CombatController getCombatController() {
        return combatController;
    }

    /**
     * 获取 AI 玩家的名称。
     */
    public String getName() {
        return fakePlayer.getName().getString();
    }

    /**
     * 每 Tick 更新。
     */
    public void tick() {
        // 更新移动控制器（跟随逻辑）
        movementController.updateFollow();

        // 更新视角控制器（看向逻辑）
        viewController.updateLook();

        // 更新交互控制器（预留扩展点）
        interactionController.tick();

        // 更新战斗控制器（预留扩展点）
        combatController.tick();
    }

    /**
     * 清理资源，移除 FakePlayer。
     */
    public void cleanup() {
        if (fakePlayer != null && !fakePlayer.isRemoved()) {
            fakePlayer.kill(net.minecraft.text.Text.literal("MineCompanion BOT removed"));
        }
    }
}

