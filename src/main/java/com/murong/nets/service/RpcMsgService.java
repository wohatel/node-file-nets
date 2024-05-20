package com.murong.nets.service;

import com.murong.nets.annotation.RpcMethod;
import com.murong.nets.client.ClientSitePool;
import com.murong.nets.client.RpcAutoReconnectClient;
import com.murong.nets.client.RpcDefaultClient;
import com.murong.nets.config.CodeConfig;
import com.murong.nets.config.EnvConfig;
import com.murong.nets.config.ExecutorPool;
import com.murong.nets.constant.FileParamConstant;
import com.murong.nets.constant.LimitMode;
import com.murong.nets.constant.RequestTypeEnmu;
import com.murong.nets.input.ExecCommandInput;
import com.murong.nets.input.ReadFileInput;
import com.murong.nets.input.RenameFileInput;
import com.murong.nets.interaction.RpcMsgTransUtil;
import com.murong.nets.interaction.RpcRequest;
import com.murong.nets.interaction.RpcResponse;
import com.murong.nets.util.CommandUtil;
import com.murong.nets.util.FileUtil;
import com.murong.nets.util.FilesTool;
import com.murong.nets.util.JsonUtil;
import com.murong.nets.util.KeyValueData;
import com.murong.nets.util.OperationMsg;
import com.murong.nets.util.RpcException;
import com.murong.nets.util.RunTimeUtil;
import com.murong.nets.util.SecureRandomUtil;
import com.murong.nets.util.StringUtil;
import com.murong.nets.util.ThreadUtil;
import com.murong.nets.vo.EnvConfVo;
import com.murong.nets.vo.FileVo;
import com.murong.nets.vo.NodeVo;
import com.murong.nets.vo.OperateSystemVo;
import com.murong.nets.vo.ReadFileVo;
import com.murong.nets.vo.WebServiceStatusVo;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class RpcMsgService {

    private static Logger logger = LoggerFactory.getLogger(RpcMsgService.class);


    private static final Map<String, Method> methods = new ConcurrentHashMap<>();

    /**
     * 初始化接口
     *
     * @param request 请求实体
     */
    public void exec(ChannelHandlerContext ctx, RpcRequest request) throws InvocationTargetException, IllegalAccessException {
        if (methods.isEmpty()) {
            List<Method> innserMethods = Arrays.stream(RpcMsgService.class.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(RpcMethod.class)).collect(Collectors.toList());

            innserMethods.forEach(m -> {
                RpcMethod annotation = m.getAnnotation(RpcMethod.class);
                String value = annotation.value();
                methods.put(value, m);
            });
        }
        Method method = methods.get(request.getRequestType());
        if (method == null) {
            return;
        }
        method.invoke(this, ctx, request);
    }

    @RpcMethod("sendFile")
    public void sendFile(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        logger.info("sendFile:{}", body);
        OperationMsg vo = new OperationMsg();
        List<String> bodyCmd = JsonUtil.parseArray(body, String.class);
        RpcDefaultClient rpcDefaultClient = ClientSitePool.getOrConnectClient(bodyCmd.get(0));
        ExecutorPool.getExecutorService().submit(() -> {
            try {
                logger.info("开始发送:{}", body);
                rpcDefaultClient.sendFile(bodyCmd.get(1), bodyCmd.get(2));
            } catch (Exception e) {
                throw new RpcException(e);
            }
        });
        if (request.isNeedResponse()) {
            RpcResponse rpcResponse = request.toResponse();
            rpcResponse.setCode(vo.getCode());
            rpcResponse.setSuccess(vo.isOperateStatus());
            rpcResponse.setMsg(vo.getMsg());
            RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
        }
    }


    @RpcMethod("sendDir")
    public void sendDir(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        OperationMsg vo = new OperationMsg();
        List<String> bodyCmd = JsonUtil.parseArray(body, String.class);
        RpcDefaultClient rpcDefaultClient = ClientSitePool.getOrConnectClient(bodyCmd.get(0));
        if (rpcDefaultClient == null) {
            vo.setCode(CodeConfig.ERROR);
            vo.setMsg("无效的命令: 目标机器链接不存在");
            vo.setOperateStatus(false);
        }
        ExecutorPool.getExecutorService().submit(() -> {
            try {
                rpcDefaultClient.sendDir(bodyCmd.get(1), bodyCmd.get(2), FileParamConstant.READ_SIZE.intValue());
            } catch (Exception e) {
                throw new RpcException(e);
            }
        });

        if (request.isNeedResponse()) {
            RpcResponse rpcResponse = request.toResponse();
            rpcResponse.setCode(vo.getCode());
            rpcResponse.setSuccess(vo.isOperateStatus());
            rpcResponse.setMsg(vo.getMsg());
            RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
        }
    }


    @RpcMethod("heart")
    public void heart(ChannelHandlerContext ctx, RpcRequest request) {
        RpcMsgTransUtil.sendMsg(ctx.channel(), request);

    }

    @RpcMethod("getNodes")
    public void getNodes(ChannelHandlerContext ctx, RpcRequest request) {
        // 最终会找到中心节点
        List<NodeVo> nodeVos = ClientSitePool.nodeList();
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setSuccess(true);
        rpcResponse.setBody(JsonUtil.toJSONString(nodeVos));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);

    }

    /**
     * 获取节点信息
     *
     * @param ctx     上下文
     * @param request 请求体
     */
    @RpcMethod("getNode")
    public void getNode(ChannelHandlerContext ctx, RpcRequest request) {
        OperationMsg vo = new OperationMsg();
        String nodeName = request.getBody();
        List<NodeVo> nodeVos = ClientSitePool.nodeList();
        NodeVo nodeVo = nodeVos.stream().filter(t -> t.getName().equals(nodeName)).findFirst().orElse(null);
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(vo.getCode());
        rpcResponse.setSuccess(vo.isOperateStatus());
        rpcResponse.setMsg(vo.getMsg());
        rpcResponse.setBody(JsonUtil.toJSONString(nodeVo));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("registerNode")
    public void registerNode(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        NodeVo nodeVo = JsonUtil.parseObject(body, NodeVo.class);
        // 优先建立连接
        ClientSitePool.accept(nodeVo);
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setSuccess(true);
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);

    }

    @RpcMethod("fileInfo")
    public void fileInfo(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        FileVo fileVo = null;
        // 优先建立连接
        File file = new File(body);
        if (file.exists()) {
            fileVo = new FileVo();
            fileVo.setDictionary(file.isDirectory());
            fileVo.setLength(file.length());
            fileVo.setName(file.getName());
            fileVo.setPath(file.getAbsolutePath());
        }
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(JsonUtil.toJSONString(fileVo));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("filesOfDir")
    public void filesOfDir(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        List<FileVo> fileVos = new ArrayList<>();
        // 优先建立连接
        File fileDir = new File(body);
        if (fileDir.exists()) {
            if (fileDir.isDirectory()) {
                File[] files = fileDir.listFiles();
                List<FileVo> collect = Arrays.stream(files).map(file -> {
                    FileVo fileVo = new FileVo();
                    fileVo.setDictionary(file.isDirectory());
                    fileVo.setLength(file.length());
                    fileVo.setName(file.getName());
                    fileVo.setPath(file.getAbsolutePath());
                    return fileVo;
                }).collect(Collectors.toList());
                fileVos.addAll(collect);
            }
        }
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(JsonUtil.toJSONString(fileVos));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("fileDelete")
    public void fileDelete(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setSuccess(true);
        rpcResponse.setCode(CodeConfig.SUCCESS);
        boolean filePathOk = EnvConfig.isFilePathOk(body);
        if (filePathOk) { // 如果文件路径在
            ThreadUtil.execSilentException(() -> {
                try {
                    FileUtils.forceDelete(new File(body));
                } catch (IOException e) {
                    throw new RpcException(e);
                }
                rpcResponse.setBody(String.valueOf(true));

            }, e -> {
                logger.error("执行错误:", e);
                rpcResponse.setBody(String.valueOf(false));
                rpcResponse.setMsg(e.getMessage());
            });
        } else {
            rpcResponse.setBody(String.valueOf(false));
            rpcResponse.setMsg("目标文件没有匹配工作目录,不可以删除");
        }
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("getHomeDirs")
    public void getHomeDirs(ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(JsonUtil.toJSONString(EnvConfig.homeDirs()));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("chHomeDirs")
    public void chHomeDirs(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        List<String> dirs = JsonUtil.parseArray(body, String.class);
        EnvConfig.clearHomeDirsAndAddAll(dirs, System.currentTimeMillis());
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(Boolean.toString(true));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("chRateLimit")
    public void chRateLimit(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        long rateLimit = Long.parseLong(body);
        // 设置限速
        EnvConfig.casRateLimit(rateLimit, System.currentTimeMillis());
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(Boolean.toString(true));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("getConf")
    public void getConf(ChannelHandlerContext ctx, RpcRequest request) {
        EnvConfVo result = new EnvConfVo();
        result.setDirsVo(EnvConfig.homeDirs());
        result.setRateLimitVo(EnvConfig.getRateLimitVo());
        ClientSitePool.nodeList();

        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(JsonUtil.toJSONString(result));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("linkedList")
    public void linkedList(ChannelHandlerContext ctx, RpcRequest request) {
        // 当前节点的连接
        this.getNodes(ctx, request);
    }

    /**
     * 重命名文件
     *
     * @param ctx     上下文
     * @param request 请求体
     */
    @RpcMethod("renameFile")
    public void renameFile(ChannelHandlerContext ctx, RpcRequest request) {
        RenameFileInput input = JsonUtil.parseObject(request.getBody(), RenameFileInput.class);
        File file = new File(input.getFile());
        RpcResponse rpcResponse = request.toResponse();
        if (!file.exists()) {
            rpcResponse.setMsg("源文件不存在");
        }
        File absoluteFile = file.getAbsoluteFile();
        File newFile = new File(absoluteFile, input.getNewName());
        try {
            FileUtils.moveFile(file, newFile);
            rpcResponse.setBody(Boolean.toString(true));
            RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
        } catch (IOException e) {
            rpcResponse.setBody(Boolean.toString(false));
            rpcResponse.setMsg(e.getMessage());
            RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
        }
    }


    @RpcMethod("cpuUsage")
    public void cpuUsage(ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(JsonUtil.toJSONString(RunTimeUtil.gainCpuUsage()));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("memoryUsage")
    public void memoryUsage(ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(JsonUtil.toJSONString(RunTimeUtil.gainMemoryUsage()));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }


    @RpcMethod("hardUsage")
    public void hardUsage(ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(JsonUtil.toJSONString(RunTimeUtil.gainHardUsage()));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("processList")
    public void processList(ChannelHandlerContext ctx, RpcRequest request) {
        String topNumber = request.getBody();
        int number = Integer.parseInt(topNumber);
        int finalNumber = number <= 0 ? 100 : number;
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(JsonUtil.toJSONString(RunTimeUtil.gainProcessList(finalNumber)));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("operabitilyCheck")
    public void operabitilyCheck(ChannelHandlerContext ctx, RpcRequest request) {
        String targetFile = request.getBody();
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        boolean checked = FileUtil.operabitilyCheck(targetFile);
        rpcResponse.setBody(String.valueOf(checked));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("clearOk")
    public void clearOk(ChannelHandlerContext ctx, RpcRequest request) {
        String targetFile = request.getBody();
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        boolean clear = FileUtil.clearOk(new File(targetFile));
        rpcResponse.setBody(String.valueOf(clear));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    @RpcMethod("operateSystemInfo")
    public void operateSystemInfo(ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        OperateSystemVo vo = new OperateSystemVo();
        vo.setOsName(System.getProperty("os.name"));
        vo.setOsVersion(System.getProperty("os.version"));
        rpcResponse.setBody(JsonUtil.toJSONString(vo));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    /**
     * 单向清理两个节点间的连接
     */
    @RpcMethod("nodeCloseConnect")
    public void nodeCloseConnect(ChannelHandlerContext ctx, RpcRequest request) {
        String targetNodeName = request.getBody();
        boolean result = ClientSitePool.closeConnect(targetNodeName);
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(String.valueOf(result));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    /**
     * 单向清理节点的所有对外的连接
     */
    @RpcMethod("nodeClearConnect")
    public void nodeClearConnect(ChannelHandlerContext ctx, RpcRequest request) {
        boolean result = ClientSitePool.closeAllConnect();
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(String.valueOf(result));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    /**
     * 中心节点分发出俩该请求
     */
    @RpcMethod("nodesDownline")
    public void nodesDownline(ChannelHandlerContext ctx, RpcRequest request) {
        List<String> nodesList = JsonUtil.parseArray(request.getBody(), String.class);
        List<String> centers = EnvConfig.getCenterNodes().stream().map(NodeVo::getName).toList();
        // 中心节点广播
        for (String nodeName : nodesList) {
            if (centers.contains(nodeName)) {
                continue;
            }
            ThreadUtil.execSilentException(() -> {
                RpcAutoReconnectClient client = ClientSitePool.get(nodeName);
                if (client != null) {
                    RpcRequest rpcRequest = new RpcRequest();
                    rpcRequest.setRequestType(RequestTypeEnmu.nodeDownline.name());
                    client.sendMsg(rpcRequest);
                }
            }, e -> logger.error("下线节点" + nodeName + "异常:", e));
        }
        // 必然下线失败的列表
        List<String> failedList = nodesList.stream().filter(nodeName -> centers.contains(nodeName) || !ClientSitePool.hasNode(nodeName)).toList();
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(JsonUtil.toJSONString(failedList));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    /**
     * 跟随节点处理下线请求,退出进程
     */
    @RpcMethod("nodeDownline")
    public void nodeDownline(ChannelHandlerContext ctx, RpcRequest request) {
        boolean isCenterNode = EnvConfig.isCenterNode();
        // 中心节点不允许下线,如果不是中心节点,则退出
        if (!isCenterNode) {
            System.exit(0);
        }
    }


    /**
     * 读取文件内容
     */
    @SneakyThrows
    @RpcMethod("readFileContent")
    public void readFileContent(ChannelHandlerContext ctx, RpcRequest request) {
        RpcResponse rpcResponse = request.toResponse();
        ReadFileInput readFileInput = JsonUtil.parseObject(request.getBody(), ReadFileInput.class);
        File file = new File(readFileInput.getFile());
        ReadFileVo readFileVo = new ReadFileVo();
        if (!file.exists()) {
            readFileVo.setErrorMsg("文件不存在");
            rpcResponse.setBody(JsonUtil.toJSONString(readFileVo));
            RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
        } else {
            KeyValueData<Long, Long, String> keyValue = FileUtil.readBytesFromPosition(file, readFileInput.getPosition(), readFileInput.getReadCharSize());
            readFileVo.setContent(keyValue.getData());
            readFileVo.setTotalByteSize(keyValue.getKey());
            readFileVo.setNextPosition(keyValue.getValue());
            rpcResponse.setBody(JsonUtil.toJSONString(readFileVo));
        }
        rpcResponse.setCode(CodeConfig.SUCCESS);
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    /**
     * 读取文件内容
     */
    @RpcMethod("execCommand")
    public void execCommand(ChannelHandlerContext ctx, RpcRequest request) throws FileNotFoundException {
        ExecCommandInput execCommandInput = JsonUtil.parseObject(request.getBody(), ExecCommandInput.class);
        String logFile = execCommandInput.getLogFile();
        File shellFile = null;
        if (execCommandInput.getExecDir() != null) {// 执行目录
            shellFile = new File(execCommandInput.getExecDir(), SecureRandomUtil.randomAlphabetic(12) + ".sh");
        } else if (!StringUtil.isBlank(logFile)) { // 日志目录
            File file = new File(logFile);
            shellFile = new File(file.getParentFile(), SecureRandomUtil.randomAlphabetic(12) + ".sh");
        } else { // 家目录
            shellFile = new File(EnvConfig.homeDirs().getDirs().get(0), SecureRandomUtil.randomAlphabetic(12) + ".sh");
        }
        // 创建shellFile
        FilesTool.createFile(shellFile);
        try (FileOutputStream outputStream = new FileOutputStream(shellFile)) {
            IOUtils.write(execCommandInput.getCommand(), outputStream, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RpcException(e);
        }
        CommandUtil.exec(shellFile.getAbsolutePath(), execCommandInput.getExecDir(), logFile, execCommandInput.getExecSecondLimit());

        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setBody(Boolean.TRUE.toString());
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }


    /**
     * 读取文件内容
     */
    @RpcMethod("webSericeAllClose")
    public void webSericeAllClose(ChannelHandlerContext ctx, RpcRequest request) {
        WebServiceStatusVo webServiceStatusVo = EnvConfig.getWebServiceStatusVo();
        webServiceStatusVo.setTime(System.currentTimeMillis());
        webServiceStatusVo.getClients().clear();
        webServiceStatusVo.setLimitMode(LimitMode.all_close);

        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setBody(Boolean.TRUE.toString());
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    /**
     * 读取文件内容
     */
    @RpcMethod("webSericeAllOpen")
    public void webSericeAllOpen(ChannelHandlerContext ctx, RpcRequest request) {
        WebServiceStatusVo webServiceStatusVo = EnvConfig.getWebServiceStatusVo();
        webServiceStatusVo.setTime(System.currentTimeMillis());
        webServiceStatusVo.getClients().clear();
        webServiceStatusVo.setLimitMode(LimitMode.all_open);

        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setBody(Boolean.TRUE.toString());
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    /**
     * 读取文件内容
     */
    @RpcMethod("webSericeSectionOpen")
    public void webSericeSectionOpen(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        List<String> nodeNames = JsonUtil.parseArray(body, String.class);
        WebServiceStatusVo webServiceStatusVo = EnvConfig.getWebServiceStatusVo();
        webServiceStatusVo.setTime(System.currentTimeMillis());
        webServiceStatusVo.getClients().clear();
        webServiceStatusVo.getClients().addAll(nodeNames);
        webServiceStatusVo.setLimitMode(LimitMode.section_open);

        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setBody(Boolean.TRUE.toString());
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

    /**
     * 读取文件内容
     */
    @RpcMethod("webSericeStatus")
    public void webSericeStatus(ChannelHandlerContext ctx, RpcRequest request) {
        WebServiceStatusVo webServiceStatusVo = EnvConfig.getWebServiceStatusVo();
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setBody(JsonUtil.toJSONString(webServiceStatusVo));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
    }

}
