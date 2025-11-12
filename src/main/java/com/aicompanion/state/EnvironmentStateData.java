package com.aicompanion.state;

/**
 * 环境状态数据类
 * 包含游戏世界的环境信息
 */
public class EnvironmentStateData {
    public String time;      // "day", "night", "sunrise", "sunset"
    public String weather;   // "clear", "rain", "thunder"
    public String biome;     // 生物群系名称

    public EnvironmentStateData() {
    }

    public EnvironmentStateData(String time, String weather, String biome) {
        this.time = time;
        this.weather = weather;
        this.biome = biome;
    }

    @Override
    public String toString() {
        return String.format("Environment{time=%s, weather=%s, biome=%s}", time, weather, biome);
    }
}
