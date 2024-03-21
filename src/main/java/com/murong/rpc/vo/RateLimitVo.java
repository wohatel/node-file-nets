package com.murong.rpc.vo;

import lombok.Data;


@Data
public class RateLimitVo {

    private long rateLimit;

    private long time;
}
