package com.aicompanion.controller;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * 移动控制器
 *
 * 负责控制 FakePlayer 的移动行为
 */
public class MovementController {

    private final EntityPlayerMPFake player;

    private Vec3d lastTargetPos = null;
    private Vec3d cachedDirection = null;
    private int pathUpdateCounter = 0;

    /**
     * 默认移动速度
     */
    private static final double DEFAULT_SPEED = 0.2;

    private static final int PATH_UPDATE_INTERVAL = 5;

    /**
     * 自动跳跃相关参数
     */
    private static final double AUTO_JUMP_CHECK_DISTANCE = 0.7;
    private static final double AUTO_JUMP_CLEARANCE_HEIGHT = 1.0;
    private static final int AUTO_JUMP_COOLDOWN_TICKS = 4;
    private int lastAutoJumpTick = -AUTO_JUMP_COOLDOWN_TICKS;

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

        tryAutoJump(direction);

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
        Vec3d currentTarget = new Vec3d(followTarget.getX(), followTarget.getY(), followTarget.getZ());

        // 如果距离太远（超过 20 格），传送过去
        if (distance > 400.0) {
            player.teleport(
                followTarget.getX(),
                followTarget.getY(),
                followTarget.getZ(),
                true
            );
            cachedDirection = null;
            lastTargetPos = null;
            pathUpdateCounter = 0;
            return;
        }

        // 如果距离超过跟随阈值，向目标移动
        if (distance > followDistance * followDistance) {
            boolean targetMovedSignificantly = lastTargetPos == null || lastTargetPos.squaredDistanceTo(currentTarget) >= 0.25;

            if (targetMovedSignificantly || pathUpdateCounter++ >= PATH_UPDATE_INTERVAL) {
                lastTargetPos = currentTarget;
                cachedDirection = currentTarget.subtract(new Vec3d(player.getX(), player.getY(), player.getZ()));
                if (cachedDirection.lengthSquared() > 0.0001) {
                    cachedDirection = cachedDirection.normalize();
                }
                pathUpdateCounter = 0;
            }

            if (cachedDirection != null && cachedDirection.lengthSquared() > 0) {
                applyCachedMovement();
            } else {
                moveTo(currentTarget);
            }
        } else {
            // 距离合适，停止移动
            stop();
            cachedDirection = null;
            pathUpdateCounter = 0;
        }
    }

    /**
     * 停止移动
     */
    public void stop() {
        player.setVelocity(Vec3d.ZERO);
    }

    private void applyCachedMovement() {
        double dx = cachedDirection.x * DEFAULT_SPEED;
        double dz = cachedDirection.z * DEFAULT_SPEED;
        tryAutoJump(cachedDirection);
        player.setVelocity(dx, player.getVelocity().y, dz);
        updateYawTowards(new Vec3d(player.getX(), player.getY(), player.getZ()).add(cachedDirection));
    }

    /**
     * 遇到正前方 1 格高方块时尝试跳跃
     */
    private void tryAutoJump(Vec3d moveDir) {
        if (moveDir == null || moveDir.lengthSquared() < 0.0001) {
            return;
        }
        if (!player.isOnGround()) {
            return;
        }

        // 冷却，避免连续起跳
        if (player.age - lastAutoJumpTick < AUTO_JUMP_COOLDOWN_TICKS) {
            return;
        }

        Vec3d direction = moveDir.normalize();
        Vec3d forwardOffset = new Vec3d(
            direction.x * AUTO_JUMP_CHECK_DISTANCE,
            0,
            direction.z * AUTO_JUMP_CHECK_DISTANCE
        );

        ServerWorld world = (ServerWorld) player.getEntityWorld();
        Box baseBox = player.getBoundingBox();
        Box forwardBox = baseBox.offset(forwardOffset);

        // 前方被挡且上方一格有空间，则起跳
        boolean blockedAhead = !world.isSpaceEmpty(player, forwardBox);
        if (blockedAhead) {
            Box upperBox = forwardBox.offset(0, AUTO_JUMP_CLEARANCE_HEIGHT, 0);
            boolean spaceAbove = world.isSpaceEmpty(player, upperBox);
            if (spaceAbove) {
                player.jump();
                lastAutoJumpTick = player.age;
            }
        }
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
