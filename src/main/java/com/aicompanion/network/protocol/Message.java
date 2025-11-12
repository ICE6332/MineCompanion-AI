package com.aicompanion.network.protocol;

/**
 * WebSocket 消息基类
 * 所有消息都遵循这个格式
 */
public class Message {
    public String type;
    public long timestamp;
    public Object data;

    public Message() {
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    public Message(String type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    @Override
    public String toString() {
        return String.format("Message{type=%s, timestamp=%d}", type, timestamp);
    }
}
