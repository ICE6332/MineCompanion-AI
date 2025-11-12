package com.aicompanion.controller;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * 视角控制器
 *
 * 负责控制 FakePlayer 的头部朝向
 */
public class ViewController {

    private final EntityPlayerMPFake player;

    /**
     * 持续看向的目标玩家
     */
    private PlayerEntity lookTarget = null;

    /**
     * 构造函数
     *
     * @param player FakePlayer 实例
     */
    public ViewController(EntityPlayerMPFake player) {
        this.player = player;
    }

    /**
     * 看向指定位置
     *
     * @param target 目标位置（世界坐标）
     */
    public void lookAt(Vec3d target) {
        Vec3d eyePos = player.getEyePos();
        Vec3d direction = target.subtract(eyePos);

        // 如果目标就在眼前，直接返回
        if (direction.lengthSquared() < 0.01) {
            return;
        }

        direction = direction.normalize();

        // 计算水平距离
        double dx = direction.x;
        double dy = direction.y;
        double dz = direction.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        // 计算偏航角（左右）
        float yaw = (float) ((Math.atan2(dz, dx) * 180.0) / Math.PI) - 90.0f;

        // 计算俯仰角（上下）
        float pitch =
            (float) -((Math.atan2(dy, horizontalDistance) * 180.0) / Math.PI);

        // 设置身体和头部朝向
        player.setYaw(yaw);
        player.setBodyYaw(yaw);
        player.setHeadYaw(yaw);
        player.setPitch(pitch);
    }

    /**
     * 看向指定实体
     *
     * @param entity 目标实体
     */
    public void lookAtEntity(Entity entity) {
        if (entity == null) {
            return;
        }
        lookAt(entity.getEyePos());
    }

    /**
     * 看向指定玩家
     *
     * @param target 目标玩家
     */
    public void lookAtPlayer(PlayerEntity target) {
        lookAtEntity(target);
    }

    /**
     * 设置持续看向的目标玩家
     *
     * @param target 目标玩家，null 表示停止看向
     */
    public void setLookTarget(PlayerEntity target) {
        this.lookTarget = target;
    }

    /**
     * 检查是否正在看向某个目标
     *
     * @return true 表示正在看向目标，false 表示未看向
     */
    public boolean isLooking() {
        return lookTarget != null;
    }

    /**
     * 更新持续看向逻辑（每 Tick 调用）
     */
    public void updateLook() {
        if (lookTarget != null && lookTarget.isAlive()) {
            lookAtPlayer(lookTarget);
        }
    }

    /**
     * 停止看向目标
     */
    public void stopLooking() {
        this.lookTarget = null;
    }
}
