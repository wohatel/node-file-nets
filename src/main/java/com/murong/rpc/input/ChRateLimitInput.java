package com.murong.rpc.input;


public class ChRateLimitInput {

    /**
     * 限速,默认无限速
     */
    private long rateLimit = Long.MAX_VALUE;

    public long getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(long rateLimit) {
        this.rateLimit = rateLimit;
    }

}
