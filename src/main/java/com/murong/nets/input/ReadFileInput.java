package com.murong.nets.input;


import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReadFileInput {

    /**
     * 源节点
     */
    @NotBlank(message = "nodeName不能为空")
    private String nodeName;

    /**
     * 文件
     */
    @NotNull(message = "file不能为空")
    private String file;

    /**
     * 本次读取的索引
     */
    @NotNull(message = "position不能为空")
    @Min(value = 0, message = "position必须>=0")
    private Long position;

    /**
     * 读取字符大小
     */
    @NotNull(message = "readCharSize不能为空")
    @Min(value = 1, message = "readCharSize必须>=1")
    @Max(1024 * 64)
    private Integer readCharSize;
}
