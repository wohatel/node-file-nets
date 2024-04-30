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
     * 开始时间
     */
    private long lastStaticTime = System.currentTimeMillis();

    /**
     * 上次统计发送的数据量,最少间隔5s
     */
    private long lastStaticSent = 0;

    /**
     * 已经发送的字节
     */
    private AtomicLong hasBeenSent = new AtomicLong(0);


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
        long interval = 5000L;
        long current = System.currentTimeMillis();
        // 开始到现在消耗的时间
        long time = current - lastStaticTime;
        long send = hasBeenSent.get();
        long expendTime = time > 0 ? time : 1;
        double currentSpeed = (send - lastStaticSent) / (expendTime + 0.0);
        if (time > interval) { //记录上个时间段段
            lastStaticTime = System.currentTimeMillis(); // 上一次统计的时间
            lastStaticSent = send; // 上一次统计时候,发送的总数
        }
        return currentSpeed;
    }

}
