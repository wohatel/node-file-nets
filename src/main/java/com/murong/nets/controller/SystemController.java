package com.murong.nets.controller;

import com.murong.nets.input.ExecCommandInput;
import com.murong.nets.service.NodeService;
import com.murong.nets.vo.CpuUsageVo;
import com.murong.nets.vo.HardUsageVo;
import com.murong.nets.vo.MemoryUsageVo;
import com.murong.nets.vo.OperateSystemVo;
import com.murong.nets.vo.ProcessActiveVo;
import com.murong.nets.vo.ResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 获取节点系统信息
 *
 * @author yaochuang 2024/04/28 17:11
 */
@RestController
@RequiredArgsConstructor
@Validated
public class SystemController {

    private final NodeService nodeService;

    /**
     * cpu使用率
     */
    @GetMapping("/system/cpuUsage")
    public ResultVo<CpuUsageVo> cpuUsage(@NotNull String nodeName) {
        return ResultVo.supplier(() -> nodeService.cpuUsage(nodeName));
    }

    /**
     * 内存使用情况
     */
    @GetMapping("/system/memoryUsage")
    public ResultVo<MemoryUsageVo> memoryUsage(@NotNull String nodeName) {
        return ResultVo.supplier(() -> nodeService.memoryUsage(nodeName));
    }

    /**
     * 硬盘使用情况
     */
    @GetMapping("/system/hardUsage")
    public ResultVo<List<HardUsageVo>> hardUsage(@NotBlank String nodeName) {
        return ResultVo.supplier(() -> nodeService.hardUsage(nodeName));
    }

    /**
     * 进程实时情况
     */
    @GetMapping("/system/processList")
    public ResultVo<List<ProcessActiveVo>> processList(@NotBlank String nodeName, @RequestParam(defaultValue = "100") Integer topNumber) {
        return ResultVo.supplier(() -> nodeService.processList(nodeName, topNumber));
    }

    /**
     * 进程实时情况
     */
    @GetMapping("/system/info")
    public ResultVo<OperateSystemVo> operateSystemInfo(@Validated @NotBlank String nodeName) {
        return ResultVo.supplier(() -> nodeService.operateSystemInfo(nodeName));
    }

    /**
     * 执行命令
     */
    @PostMapping("/system/exec/command")
    public ResultVo<Boolean> execCommand(@RequestBody @Validated ExecCommandInput input) {
        return ResultVo.supplier(() -> nodeService.execCommand(input));
    }
}
