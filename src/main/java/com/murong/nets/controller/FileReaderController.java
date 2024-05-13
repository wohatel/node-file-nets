package com.murong.nets.controller;

import com.murong.nets.input.ReadFileInput;
import com.murong.nets.service.NodeService;
import com.murong.nets.vo.ReadFileVo;
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
public class FileReaderController {

    private final NodeService nodeService;

    /**
     * 节点cp文件内容
     */
    @PostMapping("/node/read/file")
    public ResultVo<ReadFileVo> readFileContent(@RequestBody @Validated ReadFileInput input) {
        ReadFileVo readFileVo = nodeService.readFileContent(input);
        return ResultVo.supplier(() -> readFileVo);
    }

}
