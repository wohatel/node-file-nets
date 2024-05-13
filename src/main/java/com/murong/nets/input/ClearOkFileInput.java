package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ClearOkFileInput {

    /**
     * 目标节点
     */
    @NotBlank
    private String nodeName;

    /**
     * 目标文件
     */
    @NotBlank
    private String file;

}
