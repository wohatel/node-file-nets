package com.murong.nets.controller;

import com.murong.nets.config.EnvConfig;
import com.murong.nets.input.ChHomeDirInput;
import com.murong.nets.input.ChRateLimitInput;
import com.murong.nets.input.ClearNodeConnectInput;
import com.murong.nets.input.ClearOkFileInput;
import com.murong.nets.input.ColseNodeConnectInput;
import com.murong.nets.input.CpDirInput;
import com.murong.nets.input.CpFileInput;
import com.murong.nets.input.CpFileToDirInput;
import com.murong.nets.input.DelFileOrDirInput;
import com.murong.nets.input.DownlineNodeInput;
import com.murong.nets.input.RenameFileInput;
import com.murong.nets.service.NodeService;
import com.murong.nets.util.FileVerify;
import com.murong.nets.vo.DownlineNodeVo;
import com.murong.nets.vo.FileVo;
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

    /**
     * 节点cp文件内容
     */
    @PostMapping("/node/cpFile")
    public ResultVo<Boolean> cpFile(@RequestBody @Validated CpFileInput input) {
        Assert.isTrue(EnvConfig.isFilePathOk(input.getTargetFile()), "目标文件没有匹配工作目录");
        return ResultVo.supplier(() -> nodeService.cpFile(input.getSourceNode(), input.getTargetNode(), input.getSourceFile(), input.getTargetFile()));
    }

    /**
     * 节点cp文件内容
     */
    @PostMapping("/node/renameFile")
    public ResultVo<Boolean> renameFile(@RequestBody @Validated RenameFileInput input) {
        Assert.isTrue(EnvConfig.isFilePathOk(input.getNewName()), "目标文件没有匹配工作目录");
        Assert.isTrue(FileVerify.isFileNameOk(input.getNewName()), "新文件名不能包含特殊字符");
        Assert.isTrue(!input.getNewName().contains("/"), "新文件名不能包含'/'等路径符号");
        return ResultVo.supplier(() -> nodeService.renameFile(input.getNodeName(), input.getFile(), input.getNewName()));
    }

    /**
     * 文件是否空闲
     * 文件如果正被其他线程写操作,会返回false
     */
    @GetMapping("/node/file/isFree")
    public ResultVo<Boolean> operabitilyCheck(@NotBlank String nodeName, @NotBlank String file) {
        Assert.isTrue(EnvConfig.isFilePathOk(file), "目标文件没有匹配工作目录");
        return ResultVo.supplier(() -> nodeService.operabitilyCheck(nodeName, file));
    }

    /**
     * 递归清空文件夹下 .ok结尾的文件
     */
    @PostMapping("/node/file/clearOk")
    public ResultVo<Boolean> clearOk(@RequestBody @Validated ClearOkFileInput input) {
        Assert.isTrue(EnvConfig.isFilePathOk(input.getFile()), "目标文件或文件夹没有匹配工作目录");
        return ResultVo.supplier(() -> nodeService.clearOk(input.getNodeName(), input.getFile()));
    }

    /**
     * 节点cp文件到目录
     */
    @PostMapping("/node/cpFileToDir")
    public ResultVo<Boolean> cpFileToDir(@RequestBody @Validated CpFileToDirInput input) {
        String targetDir = input.getTargetDir();
        String sourceFile = input.getSourceFile();
        String sourceNode = input.getSourceNode();
        String targetNode = input.getTargetNode();
        int i = sourceFile.lastIndexOf("/");
        String fileName = sourceFile.substring(i);
        String targetPathFile = targetDir.endsWith("/") ? targetDir + fileName.substring(1) : targetDir + fileName;
        Assert.isTrue(EnvConfig.isFilePathOk(input.getTargetDir()), "目标文件夹没有匹配工作目录");
        return ResultVo.supplier(() -> nodeService.cpFile(sourceNode, targetNode, sourceFile, targetPathFile));
    }

    /**
     * 节点cp文件内容
     */
    @PostMapping("/node/cpDir")
    public ResultVo<Boolean> cpDir(@RequestBody @Validated CpDirInput input) {
        Assert.isTrue(EnvConfig.isFilePathOk(input.getTargetDir()), "目标文件夹没有匹配工作目录");
        return ResultVo.supplier(() -> nodeService.cpDir(input.getSourceNode(), input.getTargetNode(), input.getSourceDir(), input.getTargetDir()));
    }

    /**
     * 查询文件描述信息
     */
    @GetMapping("/node/fileInfo")
    public ResultVo<FileVo> fileInfo(@NotBlank String nodeName, @NotBlank String file) {
        return ResultVo.supplier(() -> nodeService.fileInfo(nodeName, file));
    }

    /**
     * 目录下所有文件
     */
    @GetMapping("/node/filesOfDir")
    public ResultVo<List<FileVo>> filesOfDir(@NotBlank String nodeName, @NotBlank String dir) {
        Assert.isTrue(EnvConfig.isFilePathOk(dir), "目标文件夹没有匹配工作目录");
        return ResultVo.supplier(() -> nodeService.filesOfDir(nodeName, dir));
    }

    /**
     * 删除文件或目录
     */
    @PostMapping("/node/fileDelete")
    public ResultVo<Boolean> fileDelete(@RequestBody @Validated DelFileOrDirInput input) {
        return ResultVo.supplier(() -> nodeService.fileDelete(input.getNodeName(), input.getFileOrDir()));
    }

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
