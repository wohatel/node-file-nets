package com.murong.nets.controller;

import com.murong.nets.config.EnvConfig;
import com.murong.nets.input.ClearOkFileInput;
import com.murong.nets.input.CpFileInput;
import com.murong.nets.input.CpFileToDirInput;
import com.murong.nets.input.DelFileOrDirInput;
import com.murong.nets.input.ReadFileInput;
import com.murong.nets.input.RenameFileInput;
import com.murong.nets.service.NodeService;
import com.murong.nets.util.FileVerify;
import com.murong.nets.util.FilesTool;
import com.murong.nets.vo.FileVo;
import com.murong.nets.vo.ReadFileVo;
import com.murong.nets.vo.ResultVo;
import com.murong.nets.vo.UploadFileVo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;


/**
 * @author yaochuang
 * @Date 2023/5/6 5:00 PM
 */
@RestController
@RequiredArgsConstructor
@Validated
public class FileController {

    private final NodeService nodeService;

    /**
     * 节点cp文件
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
        if (!EnvConfig.isFilePathOk(file)) {// 如果不是家目录文件,放权
            return ResultVo.supplier(() -> true);
        }
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
     * 读取文件内容
     */
    @PostMapping("/node/read/file")
    public ResultVo<ReadFileVo> readFileContent(@RequestBody @Validated ReadFileInput input) {
        ReadFileVo readFileVo = nodeService.readFileContent(input);
        return ResultVo.supplier(() -> readFileVo);
    }

    /**
     * 上传文件
     */
    @PostMapping("/node/upload/file")
    public ResultVo<UploadFileVo> uploadFile(@RequestPart(value = "file") MultipartFile file, @RequestParam String dir) throws IOException {
        Assert.isTrue(EnvConfig.isFilePathOk(dir), "目标文件夹没有匹配工作目录");
        File localFile = FilesTool.copyToFile(file.getInputStream(), file.getOriginalFilename(), dir);
        UploadFileVo uploadFileVo = new UploadFileVo();
        uploadFileVo.setFile(localFile.getAbsolutePath());
        uploadFileVo.setNodeName(EnvConfig.getLocalNodeName());
        uploadFileVo.setTotalByteSize(file.getSize());
        return ResultVo.supplier(() -> uploadFileVo);
    }

    /**
     * 文件下载
     */
    @GetMapping("/node/download/file")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String file) throws FileNotFoundException {
        Assert.isTrue(EnvConfig.isFilePathOk(file), "目标文件夹没有匹配工作目录");
        // 设置响应头
        int i = file.lastIndexOf("/");
        String fileName = file.substring(i + 1);
        String encoder = URLEncoder.encode(fileName, Charset.defaultCharset());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encoder);
        return new ResponseEntity<>(new InputStreamResource(new FileInputStream(file)), headers, HttpStatus.OK);
    }

}
