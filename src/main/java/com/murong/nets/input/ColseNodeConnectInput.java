package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ColseNodeConnectInput {

    /**
     * 源节点
     */
    @NotBlank
    private String sourceNode;

    /**
     * 目标节点
     */
    @NotBlank
    private String targetNode;

}
