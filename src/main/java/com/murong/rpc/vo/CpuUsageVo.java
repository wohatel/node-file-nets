package com.murong.rpc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用系统内存
 *
 * @author yaochuang 2024/04/29 12:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CpuUsageVo {

    /**
     * cpu核心数
     */
    private int coreCount;

    /**
     * cpu采样使用率
     */
    private double cpuUsage;

}
