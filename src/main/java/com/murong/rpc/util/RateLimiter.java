package com.murong.rpc.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 限速控制器
 *
 * @author yaochuang 2024/03/21 11:20
 */
public class RateLimiter {

    public RateLimiter(long rateLimit) {
        this.rateLimit = rateLimit;
    }

    /**
     * 开始时间
     */
    private long startTime = System.currentTimeMillis();

    /**
     * 已经发送的字节
     */
    private AtomicLong hasBeenSent = new AtomicLong(0);

    /**
     * 限制的速度
     */
    private long rateLimit;

    /**
     * 判断是否超速
     */
    public boolean isOverSpeed() {
        if (rateLimit <= 0) {
            return false;
        }
        return currentSpeed() > rateLimit;
    }


    /**
     * 发送的数据叠加
     */
    public RateLimiter increaseSent(long delta) {
        hasBeenSent.addAndGet(delta);
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
        long current = System.currentTimeMillis();
        // 开始到现在消耗的时间
        long time = current - startTime;
        long expendTime = time > 0 ? time : 1;
        return hasBeenSent.get() / (expendTime + 0.0);
    }

}
