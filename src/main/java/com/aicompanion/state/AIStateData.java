package com.aicompanion.state;

/**
 * AI 伙伴状态数据类
 * 包含 AI FakePlayer 的当前状态信息
 */
public class AIStateData {
    public String name;
    public Position position;
    public double health;
    public String currentAction; // 当前行为：following, idle, stopped 等

    public AIStateData() {
    }

    public AIStateData(String name, Position position, double health, String currentAction) {
        this.name = name;
        this.position = position;
        this.health = health;
        this.currentAction = currentAction;
    }

    @Override
    public String toString() {
        return String.format("AIState{name=%s, action=%s, position=%s}", name, currentAction, position);
    }
}
