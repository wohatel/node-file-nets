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
public class MemoryUsageVo {

    /**
     * 总内存
     */
    private Long totalMemory;

    /**
     * 空闲内存
     */
    private Long freeMemory;

}
