package com.murong.nets.input;


import lombok.Data;

@Data
public class InitAdminPassword {
    /**
     * 节点的leader的host
     */
    private String leaderHost;

    /**
     * 节点的leader的port
     */
    private Integer leaderPort;

    /**
     * 对称加密的密码 DES
     */
    private String password;

}
