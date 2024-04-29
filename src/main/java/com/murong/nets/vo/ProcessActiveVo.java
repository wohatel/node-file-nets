package com.murong.nets.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * 应用系统内存
 *
 * @author yaochuang 2024/04/29 12:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessActiveVo {
    private Long parentPid;
    private Long pid;
    private Duration totalCpuDuration;
    private String user;
    private String command;
    private String commandLine;
}
