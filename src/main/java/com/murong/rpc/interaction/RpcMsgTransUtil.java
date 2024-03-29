package com.murong.rpc.interaction;

import com.alibaba.fastjson.JSON;
import com.murong.rpc.config.CodeConfig;
import com.murong.rpc.config.EnvConfig;
import com.murong.rpc.util.*;
import com.murong.rpc.util.ThreadUtil;
import io.netty.channel.Channel;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

public class RpcMsgTransUtil {

    private RpcMsgTransUtil() {
    }

    public static void write(Channel channel, RpcResponse rpcResponse) {
        if (rpcResponse == null) {
            return;
        }
        channel.writeAndFlush(JSON.toJSONString(RpcMsg.build(rpcResponse)));
    }

    public static void sendMsg(Channel channel, RpcRequest rpcRequest) {
        if (rpcRequest == null) {
            return;
        }
        if (channel == null || !channel.isActive()) {
            throw new RuntimeException("连接不可用");
        }
        channel.writeAndFlush(JSON.toJSONString(RpcMsg.build(rpcRequest)));
    }

    public static RpcFuture sendSynMsg(Channel channel, RpcRequest rpcRequest) {
        RpcFuture rpcFuture = RpcInteractionContainer.addRequest(rpcRequest);
        sendMsg(channel, rpcRequest);
        return rpcFuture;
    }

    /**
     * 向服务端写文件
     *
     * @param channel
     * @param file
     * @param targetFile
     * @param
     * @throws IOException
     */
    public static void writeFile(Channel channel, String file, String targetFile, int len) throws IOException, InterruptedException {
        String fileName = file.substring(file.lastIndexOf("/"));
        String hash = DigestUtils.md5Hex(file + System.currentTimeMillis() + new Random().nextInt());
        // 1尝试发送空包,检测效果是否可以传输
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r"); FileChannel fileChannel = randomAccessFile.getChannel();) {

            long size = fileChannel.size(); //文件的总长度
            ByteBuffer byteBuffer = ByteBuffer.allocate(len);
            long position = 0;

            // 定义一个限速器
            RateLimiter rateLimiter = new RateLimiter(EnvConfig.getRateLimitVo().getRateLimit());
            while (true) {
                // 此处等待执行,如果一直不可用就一直等待
                if (!channel.isOpen()) {
                    break;
                }
                // 实时判断如果超速了,就停100ms
                if (rateLimiter.refresh(EnvConfig.getRateLimitVo().getRateLimit()).isOverSpeed()) {
                    System.out.println("当前速度:" + rateLimiter.currentSpeed());
                    Thread.sleep(100);
                    continue;
                }
                TimeUtil.execDapByFunction(() -> channel.isWritable(), 100, 100);
                int read = fileChannel.read(byteBuffer);
                if (read > 0) {
                    byteBuffer.flip();
                    int limit = byteBuffer.limit();
                    long tempPosition = position + limit; // 计算偏移后的位置
                    RpcFileRequest rpcFileRequest = new RpcFileRequest();
                    rpcFileRequest.setTargetFilePath(targetFile);
                    // 计数器计数
                    rateLimiter.increaseSent(limit);
                    if (limit == len) { //如果缓冲区填满
                        rpcFileRequest.setBytes(byteBuffer.array());
                    } else {
                        rpcFileRequest.setBytes(ArrayUtil.toBytes(byteBuffer));
                    }
                    rpcFileRequest.setFinished(tempPosition >= size); // 文件是否读取完毕
                    rpcFileRequest.setLength(size);
                    rpcFileRequest.setPosition(position);
                    rpcFileRequest.setHash(hash);
                    rpcFileRequest.setFileName(fileName);
                    RpcMsgTransUtil.sendMsg(channel, rpcFileRequest);
                    byteBuffer.clear();

                    position += limit;
                } else {
                    break;
                }
            }
        }
    }
}
