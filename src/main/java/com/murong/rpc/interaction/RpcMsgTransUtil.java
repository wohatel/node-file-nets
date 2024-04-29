package com.murong.rpc.interaction;


import com.murong.rpc.config.EnvConfig;
import com.murong.rpc.util.ArrayUtil;
import com.murong.rpc.util.JsonUtil;
import com.murong.rpc.util.RateLimiter;
import com.murong.rpc.util.RpcException;
import com.murong.rpc.util.SecureRandomUtil;
import com.murong.rpc.util.TimeUtil;
import io.netty.channel.Channel;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class RpcMsgTransUtil {

    private RpcMsgTransUtil() {
    }

    public static void write(Channel channel, RpcResponse rpcResponse) {
        if (rpcResponse == null) {
            return;
        }
        channel.writeAndFlush(JsonUtil.toJSONString(RpcMsg.build(rpcResponse)));
    }

    public static void sendMsg(Channel channel, RpcRequest rpcRequest) {
        if (rpcRequest == null) {
            return;
        }
        if (channel == null || !channel.isActive()) {
            throw new RpcException("连接不可用");
        }
        channel.writeAndFlush(JsonUtil.toJSONString(RpcMsg.build(rpcRequest)));
    }

    public static RpcFuture sendSynMsg(Channel channel, RpcRequest rpcRequest) {
        RpcFuture rpcFuture = RpcInteractionContainer.addRequest(rpcRequest);
        sendMsg(channel, rpcRequest);
        return rpcFuture;
    }

    /**
     * 向服务端写文件
     *
     * @param channel    通道
     * @param file       文件全限定名
     * @param targetFile 目标机器文件全限定名
     */
    public static void writeFile(Channel channel, String file, String targetFile, int len) throws IOException, InterruptedException {
        // 传输空文件
        if (new File(file).length() == 0L) {
            writeVoidFile(channel, file, targetFile);
            return;
        }
        String fileName = file.substring(file.lastIndexOf("/"));
        String hash = DigestUtils.md5Hex(file + System.currentTimeMillis() + SecureRandomUtil.randomInt());
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

    /**
     * 向服务端写文件
     *
     * @param channel    通道
     * @param file       孔文件名
     * @param targetFile 目标文件
     */
    public static void writeVoidFile(Channel channel, String file, String targetFile) {
        String fileName = file.substring(file.lastIndexOf("/"));
        String hash = DigestUtils.md5Hex(file + System.currentTimeMillis() + SecureRandomUtil.randomInt());
        // 1尝试发送空包,检测效果是否可以传输
        RpcFileRequest rpcFileRequest = new RpcFileRequest();
        rpcFileRequest.setTargetFilePath(targetFile);
        rpcFileRequest.setFinished(true);
        rpcFileRequest.setLength(0L);
        rpcFileRequest.setPosition(0L);
        rpcFileRequest.setHash(hash);
        rpcFileRequest.setFileName(fileName);
        RpcMsgTransUtil.sendMsg(channel, rpcFileRequest);
    }
}
