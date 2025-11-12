package com.aicompanion.player;

import carpet.patches.EntityPlayerMPFake;
import com.aicompanion.controller.MovementController;
import com.aicompanion.controller.ViewController;

/**
 * AI 玩家控制器
 *
 * 封装 FakePlayer 并提供控制接口
 */
public class AIPlayerController {

    private final EntityPlayerMPFake fakePlayer;
    private final MovementController movementController;
    private final ViewController viewController;

    /**
     * 构造函数
     *
     * @param fakePlayer Carpet Mod 的 FakePlayer 实例
     */
    public AIPlayerController(EntityPlayerMPFake fakePlayer) {
        this.fakePlayer = fakePlayer;
        this.movementController = new MovementController(fakePlayer);
        this.viewController = new ViewController(fakePlayer);
    }

    /**
     * 获取底层的 FakePlayer 实例
     *
     * @return EntityPlayerMPFake 实例
     */
    public EntityPlayerMPFake getFakePlayer() {
        return fakePlayer;
    }

    /**
     * 获取移动控制器
     *
     * @return MovementController 实例
     */
    public MovementController getMovementController() {
        return movementController;
    }

    /**
     * 获取视角控制器
     *
     * @return ViewController 实例
     */
    public ViewController getViewController() {
        return viewController;
    }

    /**
     * 获取 AI 玩家的名称
     *
     * @return 名称字符串
     */
    public String getName() {
        return fakePlayer.getName().getString();
    }

    /**
     * 每 Tick 更新
     */
    public void tick() {
        // 更新移动控制器（跟随逻辑）
        movementController.updateFollow();

        // 更新视角控制器（看向逻辑）
        viewController.updateLook();
    }

    /**
     * 清理资源，移除 FakePlayer
     */
    public void cleanup() {
        if (fakePlayer != null && !fakePlayer.isRemoved()) {
            // 使用 Carpet Mod 的 kill() 方法正确断开 FakePlayer 连接
            // 这会触发 onDisconnect() 并从玩家列表中移除
            fakePlayer.kill(net.minecraft.text.Text.literal("MineCompanion BOT removed"));
        }
    }
}

