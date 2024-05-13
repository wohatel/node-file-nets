package com.murong.nets.controller;

import com.murong.nets.config.EnvConfig;
import com.murong.nets.input.RefreshTokenInput;
import com.murong.nets.service.NodeService;
import com.murong.nets.util.RpcException;
import com.murong.nets.vo.ResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author yaochuang
 * @Date 2023/5/6 5:00 PM
 */
@RestController
@RequiredArgsConstructor
@Validated
public class AuthenticationController {

    private final NodeService nodeService;

    /**
     * 节点cp文件内容
     */
    @PostMapping("/authen/accessToken/refresh")
    public ResultVo<Boolean> readFileContent(@RequestBody @Validated RefreshTokenInput input) {
        boolean centerNode = EnvConfig.isCenterNode();
        if (!centerNode) {
            throw new RpcException("中心节点方可处理刷新token请求");
        }
        boolean approved = nodeService.refreshToken(input.getAccessToken());
        return ResultVo.supplier(() -> approved);
    }

}