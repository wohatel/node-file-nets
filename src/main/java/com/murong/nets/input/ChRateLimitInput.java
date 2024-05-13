package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class ChRateLimitInput {

    /**
     * 限速,默认无限速
     */
    @Min(1)
    private long rateLimit = Long.MAX_VALUE;

}
