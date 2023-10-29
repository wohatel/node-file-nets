package com.murong.rpc.service;

import com.alibaba.fastjson.JSON;
import com.murong.rpc.annotation.RpcMethod;
import com.murong.rpc.client.ClientSitePool;
import com.murong.rpc.client.RpcAutoReconnectClient;
import com.murong.rpc.client.RpcDefaultClient;
import com.murong.rpc.config.CodeConfig;
import com.murong.rpc.config.EnvConfig;
import com.murong.rpc.config.ExecutorPool;
import com.murong.rpc.input.RenameFileInput;
import com.murong.rpc.interaction.RpcMsgTransUtil;
import com.murong.rpc.interaction.RpcRequest;
import com.murong.rpc.interaction.RpcResponse;
import com.murong.rpc.util.JsonUtil;
import com.murong.rpc.util.OperationMsg;
import com.murong.rpc.util.ThreadUtil;
import com.murong.rpc.vo.FileVo;
import com.murong.rpc.vo.NodeVo;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class RpcMsgService {

    @Autowired
    NodeService nodeService;

    private static final Map<String, Method> methods = new ConcurrentHashMap<>();

    /**
     * 调用rpcmsg
     *
     * @param request
     */
    public static void exec(ChannelHandlerContext ctx, RpcRequest request) throws InvocationTargetException, IllegalAccessException {
        if (methods.isEmpty()) {
            List<Method> innserMethods = Arrays.stream(RpcMsgService.class.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(RpcMethod.class)).collect(Collectors.toList());

            innserMethods.stream().peek(m -> {
                RpcMethod annotation = m.getAnnotation(RpcMethod.class);
                String value = annotation.value();
                methods.put(value, m);
            }).collect(Collectors.toList());
        }
        Method method = methods.get(request.getRequestType());
        if (method == null) {
            return;
        }
        method.invoke(new RpcMsgService(), ctx, request);
    }

    @RpcMethod("sendFile")
    public void sendFile(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        OperationMsg vo = new OperationMsg();
        List<String> bodyCmd = JsonUtil.parseArray(body, String.class);
        RpcDefaultClient rpcDefaultClient = ClientSitePool.get(bodyCmd.get(0));
        if (rpcDefaultClient == null) {
            vo.setCode(CodeConfig.ERROR);
            vo.setMsg("无效的命令: 目标机器链接不存在");
            vo.setOperateStatus(false);
        }
        ExecutorPool.getExecutorService().submit(() -> {
            try {
                rpcDefaultClient.sendFile(bodyCmd.get(1), bodyCmd.get(2));
            } catch (Exception e) {
                throw new RuntimeException(e);
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
        RpcDefaultClient rpcDefaultClient = ClientSitePool.get(bodyCmd.get(0));
        if (rpcDefaultClient == null) {
            vo.setCode(CodeConfig.ERROR);
            vo.setMsg("无效的命令: 目标机器链接不存在");
            vo.setOperateStatus(false);
        }
        ExecutorPool.getExecutorService().submit(() -> {
            try {
                rpcDefaultClient.sendDir(bodyCmd.get(1), bodyCmd.get(2), 1024 * 1024);
            } catch (Exception e) {
                throw new RuntimeException(e);
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

    @RpcMethod("getNodes")
    public void getNodes(ChannelHandlerContext ctx, RpcRequest request) {
        // 最终会找到中心节点
        List<NodeVo> nodeVos = ClientSitePool.nodeList();
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setSuccess(true);
        rpcResponse.setBody(JSON.toJSONString(nodeVos));
        RpcMsgTransUtil.write(ctx.channel(), rpcResponse);

    }

    /**
     * 获取节点信息
     *
     * @param ctx
     * @param request
     */
    @RpcMethod("getNode")
    public void getNode(ChannelHandlerContext ctx, RpcRequest request) {
        OperationMsg vo = new OperationMsg();
        RpcAutoReconnectClient client = ClientSitePool.get(request.getBody());
        if (request.isNeedResponse()) {
            RpcResponse rpcResponse = request.toResponse();
            rpcResponse.setCode(vo.getCode());
            rpcResponse.setSuccess(vo.isOperateStatus());
            rpcResponse.setMsg(vo.getMsg());

            if (client != null) {
                NodeVo nodeVo = new NodeVo();
                nodeVo.setHost(client.getHost());
                nodeVo.setName(request.getBody());
                nodeVo.setPort(client.getPort());
                rpcResponse.setBody(JSON.toJSONString(nodeVo));
            }
            RpcMsgTransUtil.write(ctx.channel(), rpcResponse);
        }
    }

    @RpcMethod("registerNode")
    public void registerNode(ChannelHandlerContext ctx, RpcRequest request) {
        String body = request.getBody();
        NodeVo nodeVo = JsonUtil.parseObject(body, NodeVo.class);
        // 优先建立连接
        ClientSitePool.accept(nodeVo.getName(), nodeVo.getHost(), nodeVo.getPort());
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
                FileUtils.forceDelete(new File(body));
                rpcResponse.setBody(String.valueOf(true));

            }, e -> {
                e.printStackTrace();
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
        EnvConfig.clearHomeDirsAndAddAll(dirs);
        RpcResponse rpcResponse = request.toResponse();
        rpcResponse.setCode(CodeConfig.SUCCESS);
        rpcResponse.setBody(Boolean.toString(true));
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
     * @param ctx
     * @param request
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
}
