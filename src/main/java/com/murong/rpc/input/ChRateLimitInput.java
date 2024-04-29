package com.murong.rpc.input;

import lombok.Data;

@Data
public class ChRateLimitInput {

    /**
     * 限速,默认无限速
     */
    private long rateLimit = Long.MAX_VALUE;

}
