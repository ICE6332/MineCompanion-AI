package com.aicompanion.state;

/**
 * 玩家状态数据类
 * 包含玩家的基本状态信息，用于发送给 AI Service
 */
public class PlayerStateData {
    public String username;
    public double health;
    public int hunger;
    public Position position;
    public String dimension;
    public String gameMode;
    public String lookingAt; // 玩家正在看的内容（方块类型或实体类型）

    public PlayerStateData() {
    }

    @Override
    public String toString() {
        return String.format("PlayerState{username=%s, health=%.1f, position=%s}", username, health, position);
    }
}
