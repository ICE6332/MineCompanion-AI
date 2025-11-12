package com.aicompanion.state;

import java.util.ArrayList;
import java.util.List;

/**
 * 完整游戏状态数据类
 * 包含玩家、AI 伙伴和环境的完整状态信息
 * 用于通过 WebSocket 发送给 AI Service
 */
public class GameStateData {
    public PlayerStateData player;
    public List<AIStateData> companions;
    public EnvironmentStateData environment;
    public long timestamp; // Unix 时间戳

    public GameStateData() {
        this.companions = new ArrayList<>();
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    @Override
    public String toString() {
        return String.format("GameState{player=%s, companions=%d, environment=%s}",
            player, companions.size(), environment);
    }
}
