package com.murong.rpc.client;

import com.murong.rpc.client.handler.RpcClientRequestHandler;
import com.murong.rpc.client.handler.RpcMessageClientInteractionHandler;
import com.murong.rpc.interaction.*;
import com.murong.rpc.util.FileUtil;
import com.murong.rpc.util.RpcException;
import io.netty.channel.Channel;

import java.io.File;
import java.io.IOException;

public class SimpleRpcClient {

    protected Channel channel;

    public SimpleRpcClient() {
        this(true);
    }

    public SimpleRpcClient(boolean callGc) {
        if (callGc) {
            RpcGc.callWake();
        }
    }

    public void closeChannel() {
        if (channel != null) {
            channel.close();
        }
    }

    /**
     * 给流处理添加rpc_message处理handler
     */
    public void initRpcHandler() {
        this.channel.pipeline().addLast(new RpcMessageClientInteractionHandler());
        this.channel.pipeline().addLast(new RpcClientRequestHandler());
    }

    public Channel getChannel() {
        return channel;
    }

    /**
     * 设置channel
     *
     * @param channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
        initRpcHandler();
    }

    public void sendMsg(RpcRequest rpcRequest) {
        if (rpcRequest == null) {
            return;
        }
        if (this.channel == null || !this.channel.isActive()) {
            throw new RpcException("连接不可用");
        }
        RpcMsgTransUtil.sendMsg(channel, rpcRequest);
    }

    public RpcFuture sendSynMsg(RpcRequest rpcRequest) {
        RpcFuture rpcFuture = RpcInteractionContainer.addRequest(rpcRequest);
        rpcRequest.setNeedResponse(true);
        this.sendMsg(rpcRequest);
        return rpcFuture;
    }

    public RpcFuture sendSynMsg(RpcRequest rpcRequest, long timeOut) {
        RpcFuture rpcFuture = RpcInteractionContainer.addRequest(rpcRequest, timeOut);
        rpcRequest.setNeedResponse(true);
        this.sendMsg(rpcRequest);
        return rpcFuture;
    }

    public void sendFile(String file, String targetFile) throws IOException, InterruptedException {
        File realFile = new File(file);
        if (!realFile.exists()) {
            throw new RpcException("文件不存在");
        }
        if (!realFile.isFile()) {
            throw new RpcException("非文件传输,请检查");
        }
        RpcMsgTransUtil.writeFile(channel, file, targetFile, 128 * 1024);
    }

    public void sendFile(String file, String targetFile, int buffer) throws IOException, InterruptedException {
        RpcMsgTransUtil.writeFile(channel, file, targetFile, buffer);
    }

    /**
     * 将本机dir下的内容cp到目标机器下的某个dir下,注意本机和目标机的目录对等的
     * 注意隐藏目录不会cp
     *
     * @param fromDir   本机环境的dir
     * @param targetDir 目标机器的dir
     * @param buffer    一次发送的块大小
     */
    public void sendDir(String fromDir, String targetDir, int buffer) throws IOException, InterruptedException {
        File localDir = new File(fromDir);
        if (!localDir.exists()) {
            throw new RpcException("源目录不存在");
        }
        if (!localDir.isDirectory()) {
            throw new RpcException("源目录路径错误,注意是目录");
        }
        File[] files = localDir.listFiles();
        for (File file : files) {
            String equalPath = FileUtil.findEqualPath(fromDir, targetDir, file);
            if (file.isFile()) { // 如果是文件,则直接传输
                sendFile(file.getAbsolutePath(), equalPath, buffer);
            }
            if (file.isDirectory()) {
                sendDir(file.getAbsolutePath(), equalPath, buffer);
            }
        }

    }
}
