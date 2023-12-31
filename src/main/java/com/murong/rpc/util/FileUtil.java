package com.murong.rpc.util;


import com.murong.rpc.config.CodeConfig;
import com.murong.rpc.config.EnvConfig;
import com.murong.rpc.config.ExecutorPool;
import com.murong.rpc.interaction.RpcFileRequest;
import com.murong.rpc.interaction.RpcResponse;
import io.netty.util.internal.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileUtil {

    /**
     * 文件处理
     */
    public static Map<String, KeyValue<String, FileChannel, Long>> channelMap = new ConcurrentHashMap<>();

    /**
     * 默认处于关闭中,上下文可见
     */
    private volatile static boolean clearing = false;

    /**
     * 文件追加或插入
     *
     * @param file
     * @param bytes
     * @param startPosition
     * @throws IOException
     */
    public static void appendFile(String file, byte[] bytes, long startPosition) throws IOException {
        try (FileChannel channel = FileChannel.open(Paths.get(file), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            channel.write(byteBuffer, startPosition);
        }
    }

    /**
     * 文件追加到末尾
     *
     * @param file
     * @param bytes
     * @throws IOException
     */
    public static void appendFile(String file, byte[] bytes) throws IOException {
        try (FileChannel channel = FileChannel.open(Paths.get(file), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            channel.write(byteBuffer);
        }
    }

    /**
     * md5文件hash
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String fileMd5Hash(String file) {
        try {
            return DigestUtils.md5Hex(new FileInputStream(file));
        } catch (IOException e) {
        }
        return null;
    }

    public static void appendFile(RpcFileRequest rpcFileRequest) throws IOException {
        boolean contains = channelMap.containsKey(rpcFileRequest.getHash());
        // 1尝试创建文件夹
        String targetFilePath = rpcFileRequest.getTargetFilePath();
        int i = targetFilePath.lastIndexOf("/");
        String substring = targetFilePath.substring(0, i);
        File file = new File(substring);
        if (!file.exists()) {
            file.mkdirs();
        }
        FileChannel fileChannel = null;
        if (contains) {
            KeyValue<String, FileChannel, Long> keyValue = channelMap.get(rpcFileRequest.getHash());
            keyValue.setData(System.currentTimeMillis()); // 设置时间
            fileChannel = keyValue.getValue();
        } else {
            fileChannel = FileChannel.open(Paths.get(rpcFileRequest.getTargetFilePath()), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            KeyValue<String, FileChannel, Long> keyValue = new KeyValue<>(rpcFileRequest.getHash(), fileChannel, System.currentTimeMillis());
            channelMap.put(rpcFileRequest.getHash(), keyValue);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(rpcFileRequest.getBytes());
        fileChannel.write(byteBuffer);
    }

    public static RpcResponse dealRpcFileRequest(RpcFileRequest rpcFileRequest) throws IOException {
        RpcResponse rpcResponse = rpcFileRequest.toResponse();
        try {
            appendFile(rpcFileRequest);
        } catch (Exception e) {
            rpcResponse.setMsg(e.getMessage());
            rpcResponse.setCode(CodeConfig.ERROR);
            rpcResponse.setSuccess(false);
        } finally {
            clearFileChannel();
        }
        return rpcResponse;
    }

    /**
     * 关闭流
     */
    public static void clearFileChannel() {
        if (clearing) {
            return;
        }
        ExecutorPool.getExecutorService().submit(() -> {
            if (clearing) {
                return;
            }
            clearing = true; // 占位
            try {
                TimeUtil.execDapByFunction(() -> {
                    Iterator<Map.Entry<String, KeyValue<String, FileChannel, Long>>> iterator = channelMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, KeyValue<String, FileChannel, Long>> next = iterator.next();
                        KeyValue<String, FileChannel, Long> value = next.getValue();
                        Long time = value.getData();
                        long current = System.currentTimeMillis();
                        if (current - time > 20 * 1000) { // 如果超过20s的时间没有活动,则认为文件已处理完成
                            try {
                                FileChannel fileChannel = value.getValue();
                                fileChannel.close();
                            } catch (Exception e) {
                            }
                            iterator.remove();
                        }
                    }
                    return channelMap.size() <= 0; // 如果为结果小于=0,则说明没有流操作了
                }, 10 * 1000l, 500); // 这就要求文件不能太大了
            } finally {
                clearing = false; // 释放
                System.out.println("释放链接");
            }
        });

    }

    /**
     * @param fromPath       本机文件的path
     * @param toPath         目标机文件的path
     * @param fileInFromPath 本机文件path目录下的文件
     * @return 目标机对应的文件
     */
    public static String findEqualPath(String fromPath, String toPath, File fileInFromPath) {
        if (fromPath.endsWith("/")) {
            fromPath = fromPath.substring(0, fromPath.length() - 1);
        }
        if (toPath.endsWith("/")) {
            toPath = toPath.substring(0, toPath.length() - 1);
        }
        String absolutePath = fileInFromPath.getAbsolutePath();
        String substring = absolutePath.substring(fromPath.length());
        return toPath + substring;
    }

}