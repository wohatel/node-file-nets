package com.murong.nets.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * description
 *
 * @author yaochuang 2024/05/13 13:55
 */
@Getter
@Setter
public class AuthenAccessVo {

    /**
     * 项目token
     */
    private String accessToken;

    /**
     * token刷新时间
     */
    private LocalDateTime refreshTime;

}
