package com.murong.nets.vo;

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
public class HardUsageVo {

    /**
     * 路径
     */
    private String drive;

    /**
     * 总容量
     */
    private Long totalSpace;

    /**
     * 空闲空间
     */
    private Long freeSpace;

    /**
     * 可用空间
     */
    private Long usableSpace;


}
