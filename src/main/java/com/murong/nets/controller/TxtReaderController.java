package com.murong.nets.controller;

import com.murong.nets.constant.FileParamConstant;
import com.murong.nets.input.ReadFileInput;
import com.murong.nets.service.NodeService;
import com.murong.nets.vo.ReadFileVo;
import com.murong.nets.vo.ResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author yaochuang
 * @Date 2023/5/6 5:00 PM
 */
@RestController
@RequiredArgsConstructor
public class TxtReaderController {

    private final NodeService nodeService;

    /**
     * 节点cp文件内容
     */
    @PostMapping("/node/read/file")
    public ResultVo<ReadFileVo> readFileContent(@RequestBody ReadFileInput input) {
        Assert.hasLength(input.getFile(), "文件不能为空");
        Assert.notNull(input.getNodeName(), "节点不能为空");
        Assert.notNull(input.getReadSize(), "本次读取大小不能为空");
        Assert.notNull(input.getPosition(), "读取位置索引参数错误");
        Assert.isTrue(input.getReadSize() <= FileParamConstant.READ_SIZE, "一次读取大小不能超过64k");
        ReadFileVo readFileVo = nodeService.readFileContent(input);
        return ResultVo.supplier(() -> readFileVo);
    }

}
