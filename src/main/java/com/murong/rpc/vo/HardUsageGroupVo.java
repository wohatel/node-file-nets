package com.murong.rpc.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 应用系统内存
 *
 * @author yaochuang 2024/04/29 12:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HardUsageGroupVo {

    private List<HardUsageVo> hardUsageList;

}
