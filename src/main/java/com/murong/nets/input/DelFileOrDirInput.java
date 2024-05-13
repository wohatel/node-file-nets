package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 获取节点的文件信息
 */
@Data
public class DelFileOrDirInput {

    /**
     * 源节点
     */
    @NotBlank
    private String nodeName;

    /**
     * 目标文件或文件夹
     */
    @NotBlank
    private String fileOrDir;


}
