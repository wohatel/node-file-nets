package com.murong.nets.controller;

import com.murong.nets.service.NodeService;
import com.murong.nets.vo.CpuUsageVo;
import com.murong.nets.vo.HardUsageVo;
import com.murong.nets.vo.MemoryUsageVo;
import com.murong.nets.vo.ProcessActiveVo;
import com.murong.nets.vo.ResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 获取节点系统信息
 *
 * @author yaochuang 2024/04/28 17:11
 */
@RestController
@RequiredArgsConstructor
public class SystemController {

    private final NodeService nodeService;

    /**
     * 查询所有的节点
     */
    @GetMapping("/system/cpuUsage")
    public ResultVo<CpuUsageVo> cpuUsage(@RequestParam String nodeName) {
        return ResultVo.supplier(() -> nodeService.cpuUsage(nodeName));
    }

    /**
     * 查询所有的节点
     */
    @GetMapping("/system/memoryUsage")
    public ResultVo<MemoryUsageVo> memoryUsage(@RequestParam String nodeName) {
        return ResultVo.supplier(() -> nodeService.memoryUsage(nodeName));
    }

    /**
     * 查询所有的节点
     */
    @GetMapping("/system/hardUsage")
    public ResultVo<List<HardUsageVo>> hardUsage(@RequestParam String nodeName) {
        return ResultVo.supplier(() -> nodeService.hardUsage(nodeName));
    }

    /**
     * 查询所有的节点
     */
    @GetMapping("/system/processList")
    public ResultVo<List<ProcessActiveVo>> processList(@RequestParam String nodeName, @RequestParam(defaultValue = "100") Integer topNumber) {
        return ResultVo.supplier(() -> nodeService.processList(nodeName, topNumber));
    }

}
