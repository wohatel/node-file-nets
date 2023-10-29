package com.murong.rpc.input;


import com.murong.rpc.util.MD5Util;

import java.security.MessageDigest;

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


    public String getLeaderHost() {
        return leaderHost;
    }

    public void setLeaderHost(String leaderHost) {
        this.leaderHost = leaderHost;
    }

    public Integer getLeaderPort() {
        return leaderPort;
    }

    public void setLeaderPort(Integer leaderPort) {
        this.leaderPort = leaderPort;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
