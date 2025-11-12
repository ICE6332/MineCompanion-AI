package com.aicompanion.state;

/**
 * 3D 坐标位置数据类
 * 用于在 WebSocket 消息中传输位置信息
 */
public class Position {
    public double x;
    public double y;
    public double z;

    public Position() {
    }

    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return String.format("Position{x=%.2f, y=%.2f, z=%.2f}", x, y, z);
    }
}
