package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * description
 *
 * @author yaochuang 2024/05/16 15:09
 */
@Data
public class ExecCommandInput {
    /**
     * 节点名称
     */
    @NotBlank
    private String nodeName;

    /**
     * 执行的命令
     */
    @NotBlank
    private String command;

    /**
     * 执行命令的目录
     */
    private String execDir;

    /**
     * 执行时间限制0: 不做处理
     * >0 到到达时间后停止该shell进程
     */
    private long execSecondLimit;

    /**
     * 输出的日志文件
     * 如果没有填写,表示没有日志
     */
    private String logFile;
}
