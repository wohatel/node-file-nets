package com.murong.nets.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 限速控制器
 *
 * @author yaochuang 2024/03/21 11:20
 */
public class RateLimiter {

    /**
     * 限制的速度
     * 单位 kb
     */
    private long rateLimit;

    /**
     * 开始记录
     */
    private KeyValue<Long, Long, Long> firstRecord = new KeyValue<>(0L, System.currentTimeMillis());

    /**
     * 第二记录
     */
    private KeyValue<Long, Long, Long> secondRecord = new KeyValue<>(0L, System.currentTimeMillis());

    /**
     * 已经发送的字节
     */
    private final AtomicLong hasBeenSent = new AtomicLong(0);


    public RateLimiter(long rateLimit) {
        this.rateLimit = rateLimit;
    }

    /**
     * 判断是否超速
     */
    public boolean isOverSpeed() {
        if (rateLimit <= 0) {
            return false;
        }
        // 为了防止速度过慢,导致文件不再传输问题
        long currentRate = rateLimit > 32 ? rateLimit : 32;
        return currentSpeed() > currentRate;
    }


    /**
     * 发送的数据叠加
     */
    public RateLimiter increaseSent(long delta) {
        long send = hasBeenSent.addAndGet(delta);
        long currentTime = System.currentTimeMillis();
        if (System.currentTimeMillis() - secondRecord.getValue() > 3000L) {
            firstRecord.setKey(secondRecord.getKey());
            firstRecord.setValue(secondRecord.getValue());

            secondRecord.setKey(send);
            secondRecord.setValue(currentTime);
        }
        return this;
    }

    /**
     * 限速设置
     */
    public RateLimiter refresh(long rateLimit) {
        this.rateLimit = rateLimit;
        return this;
    }

    /**
     * 获取实时速度
     */
    public double currentSpeed() {
        long currentTime = System.currentTimeMillis();
        long lastTime = firstRecord.getValue();

        long intervalTime = currentTime - lastTime;
        long expendTime = intervalTime > 0 ? intervalTime : 1;

        long currentSend = hasBeenSent.get();
        long lastSend = firstRecord.getKey();
        return (currentSend - lastSend) / (expendTime + 0.0);
    }

}
