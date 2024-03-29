package com.murong.rpc.input;

import lombok.Data;

import java.util.List;

@Data
public class ChHomeDirInput {

    /**
     * 源节点
     */
    private List<String> homeDirs;

    public List<String> getHomeDirs() {
        return homeDirs;
    }

    public void setHomeDirs(List<String> homeDirs) {
        this.homeDirs = homeDirs;
    }
}
