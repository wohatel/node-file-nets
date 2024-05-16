package com.murong.nets.controller;

import com.murong.nets.input.ClearNodeConnectInput;
import com.murong.nets.input.ColseNodeConnectInput;
import com.murong.nets.input.DownlineNodeInput;
import com.murong.nets.service.NodeService;
import com.murong.nets.vo.DownlineNodeVo;
import com.murong.nets.vo.NodeVo;
import com.murong.nets.vo.ResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * @author yaochuang
 * @Date 2023/5/6 5:00 PM
 */
@RestController
@RequiredArgsConstructor
@Validated
public class NodeController {

    private final NodeService nodeService;

    /**
     * 查询所有的节点
     */
    @GetMapping("/node/list")
    public ResultVo<List<NodeVo>> nodeList() {
        return ResultVo.supplier(nodeService::nodeList);
    }

    /**
     * 查询节点连接情况
     */
    @GetMapping("/node/linkedList")
    public ResultVo<List<NodeVo>> nodeList(@NotBlank String nodeName) {
        return ResultVo.supplier(() -> nodeService.linkedList(nodeName));
    }

    /**
     * 关闭连接的节点
     */
    @PostMapping("/node/close/connect")
    public ResultVo<Boolean> nodeCloseConnect(@RequestBody @Validated ColseNodeConnectInput input) {
        return ResultVo.supplier(() -> nodeService.nodeCloseConnect(input.getSourceNode(), input.getTargetNode()));
    }

    /**
     * 清理所有链接
     */
    @PostMapping("/node/close/all/connect")
    public ResultVo<Boolean> nodeClearConnect(@RequestBody @Validated ClearNodeConnectInput input) {
        Assert.hasLength(input.getNodeName(), "节点名参数有误");
        return ResultVo.supplier(() -> nodeService.nodeClearConnect(input.getNodeName()));
    }

    /**
     * 剔除掉某些节点,节点退出进程
     */
    @PostMapping("/node/downline")
    public ResultVo<DownlineNodeVo> nodesDownline(@RequestBody @Validated DownlineNodeInput input) {
        List<String> failNodes = nodeService.nodesDownline(input.getNodeNames());
        DownlineNodeVo downlineNodeVo = new DownlineNodeVo();
        downlineNodeVo.setFialedNodeNames(failNodes);
        return ResultVo.supplier(() -> downlineNodeVo);
    }

}
