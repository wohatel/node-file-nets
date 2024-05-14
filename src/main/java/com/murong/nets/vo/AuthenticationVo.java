package com.murong.nets.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author yaochuang 2024/05/13 13:55
 */
@Data
public class AuthenticationVo extends AuthenAccessVo {

    /**
     * 签发时间
     */
    private LocalDateTime signTime;

    /**
     * 默认最大数量
     */
    private Integer nodeMax = Integer.MAX_VALUE;

    /**
     * 失效时间默认7天
     */
    private LocalDateTime expireTime = LocalDateTime.now().plusDays(5);

    /**
     * 中心节点
     */
    private List<String> accessHostPorts = new ArrayList<>();
}
