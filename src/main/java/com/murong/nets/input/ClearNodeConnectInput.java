package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ClearNodeConnectInput {

    /**
     * 节点名称
     */
    @NotBlank(message = "节点参数不能为空")
    private String nodeName;

}
