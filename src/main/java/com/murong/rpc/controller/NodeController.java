package com.murong.rpc.controller;

import com.murong.rpc.config.EnvConfig;
import com.murong.rpc.input.*;
import com.murong.rpc.service.NodeService;
import com.murong.rpc.util.FileVerify;
import com.murong.rpc.util.StringUtil;
import com.murong.rpc.vo.FileVo;
import com.murong.rpc.vo.NodeVo;
import com.murong.rpc.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author yaochuang
 * @Date 2023/5/6 5:00 PM
 */
@RestController
public class NodeController {

    @Autowired
    NodeService nodeService;

    /**
     * 查询所有的节点
     */
    @GetMapping("/node/list")
    public ResultVo<List<NodeVo>> nodeList() {
        return ResultVo.supplier(() -> nodeService.nodeList());
    }

    /**
     * 查询节点连接情况
     */
    @GetMapping("/node/linkedList")
    public ResultVo<List<NodeVo>> nodeList(String nodeName) {
        Assert.isTrue(!StringUtil.isBlank(nodeName), "节点名参数有误");
        return ResultVo.supplier(() -> nodeService.linkedList(nodeName));
    }

    /**
     * 节点cp文件内容
     */
    @PostMapping("/node/cpFile")
    public ResultVo<Boolean> cpFile(@RequestBody CpFileInput input) {
        Assert.isTrue(EnvConfig.isFilePathOk(input.getTargetFile()), "目标文件没有匹配工作目录");
        return ResultVo.supplier(() -> nodeService.cpFile(input.getSourceNode(), input.getTargetNode(), input.getSourceFile(), input.getTargetFile()));
    }

    /**
     * 节点cp文件内容
     */
    @PostMapping("/node/renameFile")
    public ResultVo<Boolean> renameFile(@RequestBody RenameFileInput input) {
        Assert.isTrue(EnvConfig.isFilePathOk(input.getNewName()), "新文件名不能为空");
        Assert.isTrue(FileVerify.isFileNameOk(input.getNewName()), "新文件名不能包含特殊字符");
        return ResultVo.supplier(() -> nodeService.renameFile(input.getNodeName(), input.getFile(), input.getNewName()));
    }

    /**
     * 节点cp文件到目录
     */
    @PostMapping("/node/cpFileToDir")
    public ResultVo<Boolean> cpFileToDir(@RequestBody CpFileToDirInput input) {
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
    public ResultVo<Boolean> cpDir(@RequestBody CpDirInput input) {
        Assert.isTrue(EnvConfig.isFilePathOk(input.getTargetDir()), "目标文件夹没有匹配工作目录");
        return ResultVo.supplier(() -> nodeService.cpDir(input.getSourceNode(), input.getTargetNode(), input.getSourceDir(), input.getTargetDir()));
    }

    /**
     * 查询文件描述信息
     */
    @PostMapping("/node/fileInfo")
    public ResultVo<FileVo> fileInfo(@RequestBody GetFileInfoInput input) {
        Assert.isTrue(!StringUtil.isBlank(input.getFile()), "文件路径参数错误");
        Assert.isTrue(!StringUtil.isBlank(input.getNodeName()), "节点名参数错误");
        return ResultVo.supplier(() -> nodeService.fileInfo(input.getNodeName(), input.getFile()));
    }

    /**
     * 文件下的所有目录
     */
    @PostMapping("/node/filesOfDir")
    public ResultVo<List<FileVo>> filesOfDir(@RequestBody GetFileOfDirInput input) {
        Assert.isTrue(!StringUtil.isBlank(input.getDir()), "文件夹路径参数错误");
        Assert.isTrue(!StringUtil.isBlank(input.getNodeName()), "节点名参数错误");
        Assert.isTrue(EnvConfig.isFilePathOk(input.getDir()), "目标文件夹没有匹配工作目录");
        return ResultVo.supplier(() -> nodeService.filesOfDir(input.getNodeName(), input.getDir()));
    }

    /**
     * 删除文件或目录
     */
    @PostMapping("/node/fileDelete")
    public ResultVo<Boolean> fileDelete(@RequestBody DelFileOrDirInput input) {
        Assert.isTrue(!StringUtil.isBlank(input.getFileOrDir()), "文件路径参数错误");
        Assert.isTrue(!StringUtil.isBlank(input.getNodeName()), "节点名参数错误");
        return ResultVo.supplier(() -> nodeService.fileDelete(input.getNodeName(), input.getFileOrDir()));
    }

    /**
     * 变更工作目录
     */
    @PostMapping("/node/chHomeDirs")
    public ResultVo<Boolean> chHomeDirs(@RequestBody ChHomeDirInput input) {
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
     */
    @PostMapping("/node/chRateLimit")
    public ResultVo<Boolean> chRateLimit(@RequestBody ChRateLimitInput input) {
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
