package com.aicompanion.controller;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * 移动控制器
 *
 * 负责控制 FakePlayer 的移动行为
 */
public class MovementController {

    private final EntityPlayerMPFake player;

    /**
     * 默认移动速度
     */
    private static final double DEFAULT_SPEED = 0.2;

    /**
     * 跟随目标玩家
     */
    private ServerPlayerEntity followTarget = null;

    /**
     * 跟随距离阈值
     */
    private double followDistance = 3.0;

    /**
     * 构造函数
     *
     * @param player FakePlayer 实例
     */
    public MovementController(EntityPlayerMPFake player) {
        this.player = player;
    }

    /**
     * 移动到指定位置
     *
     * @param target 目标位置
     */
    public void moveTo(Vec3d target) {
        moveTo(target, DEFAULT_SPEED);
    }

    /**
     * 以指定速度移动到目标位置
     *
     * @param target 目标位置
     * @param speed 移动速度
     */
    public void moveTo(Vec3d target, double speed) {
        Vec3d current = new Vec3d(player.getX(), player.getY(), player.getZ());
        Vec3d direction = target.subtract(current);

        // 如果已经很接近目标，停止移动
        if (direction.length() < 0.5) {
            stop();
            return;
        }

        // 归一化方向向量
        direction = direction.normalize();

        // 只在水平方向移动，保持 Y 轴速度（重力）
        double dx = direction.x * speed;
        double dz = direction.z * speed;

        player.setVelocity(dx, player.getVelocity().y, dz);

        // 更新朝向
        updateYawTowards(target);
    }

    /**
     * 移动到指定玩家的位置
     *
     * @param target 目标玩家
     */
    public void moveToPlayer(PlayerEntity target) {
        moveTo(new Vec3d(target.getX(), target.getY(), target.getZ()));
    }

    /**
     * 设置跟随目标玩家
     *
     * @param target 目标玩家，null 表示停止跟随
     */
    public void setFollowTarget(ServerPlayerEntity target) {
        this.followTarget = target;
    }

    /**
     * 获取当前跟随的目标玩家
     *
     * @return 目标玩家，null 表示未跟随任何人
     */
    public ServerPlayerEntity getFollowTarget() {
        return followTarget;
    }

    /**
     * 检查是否正在跟随某个玩家
     *
     * @return true 表示正在跟随，false 表示未跟随
     */
    public boolean isFollowing() {
        return followTarget != null;
    }

    /**
     * 设置跟随距离
     *
     * @param distance 距离（格）
     */
    public void setFollowDistance(double distance) {
        this.followDistance = distance;
    }

    /**
     * 让 FakePlayer 跳跃。
     */
    public void jump() {
        player.jump();
    }

    /**
     * 更新跟随逻辑（每 Tick 调用）
     */
    public void updateFollow() {
        if (followTarget == null || !followTarget.isAlive()) {
            return;
        }

        double distance = player.squaredDistanceTo(followTarget);

        // 如果距离太远（超过 20 格），传送过去
        if (distance > 400.0) {
            player.teleport(
                followTarget.getX(),
                followTarget.getY(),
                followTarget.getZ(),
                true
            );
            return;
        }

        // 如果距离超过跟随阈值，向目标移动
        if (distance > followDistance * followDistance) {
            moveTo(new Vec3d(
                followTarget.getX(),
                followTarget.getY(),
                followTarget.getZ()
            ));
        } else {
            // 距离合适，停止移动
            stop();
        }
    }

    /**
     * 停止移动
     */
    public void stop() {
        player.setVelocity(Vec3d.ZERO);
    }

    /**
     * 更新朝向以面向目标位置
     *
     * @param target 目标位置
     */
    private void updateYawTowards(Vec3d target) {
        double dx = target.x - player.getX();
        double dz = target.z - player.getZ();

        // 计算偏航角（左右）
        float yaw = (float) ((Math.atan2(dz, dx) * 180.0) / Math.PI) - 90.0f;
        player.setYaw(yaw);
        player.setBodyYaw(yaw);
    }
}
