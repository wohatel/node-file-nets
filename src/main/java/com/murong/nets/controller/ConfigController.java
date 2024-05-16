package com.murong.nets.controller;

import com.murong.nets.config.EnvConfig;
import com.murong.nets.input.ChHomeDirInput;
import com.murong.nets.input.ChRateLimitInput;
import com.murong.nets.service.NodeService;
import com.murong.nets.vo.ResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class ConfigController {


    private final NodeService nodeService;

    /**
     * 变更工作目录
     */
    @PostMapping("/node/chHomeDirs")
    public ResultVo<Boolean> chHomeDirs(@RequestBody @Validated ChHomeDirInput input) {
        return ResultVo.supplier(() -> nodeService.chHomeDirs(input.getHomeDirs()));
    }

    /**
     * 获取家目录
     */
    @GetMapping("/node/homeDirs")
    public ResultVo<List<String>> homeDirs() {
        return ResultVo.supplier(() -> EnvConfig.homeDirs().getDirs());
    }

    /**
     * 变更文件传输速度
     * 单位kb
     */
    @PostMapping("/node/chRateLimit")
    public ResultVo<Boolean> chRateLimit(@RequestBody @Validated ChRateLimitInput input) {
        return ResultVo.supplier(() -> nodeService.chRateLimit(input.getRateLimit()));
    }

    /**
     * 获取文件传输速度
     */
    @GetMapping("/node/rateLimit")
    public ResultVo<Long> rateLimit() {
        return ResultVo.supplier(() -> EnvConfig.getRateLimitVo().getRateLimit());
    }
}
