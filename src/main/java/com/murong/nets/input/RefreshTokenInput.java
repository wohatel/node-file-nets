package com.murong.nets.input;


import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RefreshTokenInput {

    /**
     * 集群的认证token
     */
    @NotBlank
    private String accessToken;
}
