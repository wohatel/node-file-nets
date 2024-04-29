package com.murong.nets.vo;

import lombok.Data;

import java.util.List;

/**
 * description
 *
 * @author yaochuang 2024/03/21 10:45
 */
@Data
public class EnvConfVo {

    private RateLimitVo rateLimitVo;

    private DirsVo dirsVo;

    private List<NodeVo> nodeVoList;
}
