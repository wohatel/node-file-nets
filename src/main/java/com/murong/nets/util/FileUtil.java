package com.murong.nets.util;


import com.murong.nets.config.CodeConfig;
import com.murong.nets.config.ExecutorPool;
import com.murong.nets.interaction.RpcFileRequest;
import com.murong.nets.interaction.RpcResponse;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FileUtil {

    /**
     * 文件处理
     * node2 keyValue: 1文件传输的大小, FileChannel 文件打开的通道, Long当前时间
     */
    private static Map<String, KeyValue<AtomicLong, FileChannel, Long>> channelMap = new ConcurrentHashMap<>();

    /**
     * 默认处于关闭中,上下文可见
     */
    private volatile static boolean clearing = false;

    /**
     * 文件追加或插入
     *
     * @param file          文件全路径名
     * @param bytes         字节数
     * @param startPosition 开始position
     * @throws IOException 抛出的异常
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
     * @param file  文件名
     * @param bytes 字节
     * @throws IOException 抛出异常
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
     * @param file 文件名
     * @return String 文件hash
     * @throws IOException 抛出异常
     */
    public static String fileMd5Hash(String file) {
        try {
            return DigestUtils.md5Hex(new FileInputStream(file));
        } catch (IOException e) {
        }
        return null;
    }

    public static boolean appendFile(RpcFileRequest rpcFileRequest) throws IOException {
        // 说明是首次上传,则应该是先删除
        boolean contains = channelMap.containsKey(rpcFileRequest.getHash());
        KeyValue<AtomicLong, FileChannel, Long> keyValue = null;
        if (contains) {
            keyValue = channelMap.get(rpcFileRequest.getHash());
            keyValue.setData(System.currentTimeMillis()); // 设置时间
            // 总数
            AtomicLong key = keyValue.getKey();
            key.addAndGet(rpcFileRequest.getBytes().length);
        } else {
            reCreateFile(rpcFileRequest.getTargetFilePath());
            // 尝试创建父目录
            FileChannel fileChannel = FileChannel.open(Paths.get(rpcFileRequest.getTargetFilePath()), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            keyValue = new KeyValue<>(new AtomicLong(rpcFileRequest.getBytes().length), fileChannel, System.currentTimeMillis(), rpcFileRequest.getTargetFilePath());
            channelMap.put(rpcFileRequest.getHash(), keyValue);
        }
        FileChannel fileChannel = keyValue.getValue();
        ByteBuffer byteBuffer = ByteBuffer.wrap(rpcFileRequest.getBytes());
        fileChannel.write(byteBuffer);
        return keyValue.getKey().get() >= rpcFileRequest.getLength();
    }

    public static RpcResponse dealRpcFileRequest(RpcFileRequest rpcFileRequest) throws IOException {
        RpcResponse rpcResponse = rpcFileRequest.toResponse();
        try {
            // 空文件处理
            boolean ifVoid = rpcFileRequest.getLength() == 0;
            if (ifVoid) {
                reCreateFile(rpcFileRequest.getFileName());
                reCreateFile(rpcFileRequest.getFileName() + ".ok");
            } else {
                File okfile = new File(rpcFileRequest.getTargetFilePath() + ".ok");
                Files.deleteIfExists(okfile.toPath());
                boolean isOver = appendFile(rpcFileRequest);
                if (isOver) {
                    FileUtil.release(rpcFileRequest.getHash());
                    Files.createFile(okfile.toPath());
                }
            }
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
                    Iterator<Map.Entry<String, KeyValue<AtomicLong, FileChannel, Long>>> iterator = channelMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, KeyValue<AtomicLong, FileChannel, Long>> next = iterator.next();
                        KeyValue<AtomicLong, FileChannel, Long> value = next.getValue();
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
                }, 10 * 1000L, 500); // 这就要求文件不能太大了
            } finally {
                clearing = false; // 释放
            }
        });

    }

    /**
     * 关闭流
     *
     * @param fileHash 文件hash
     */
    public static void release(String fileHash) {
        KeyValue<AtomicLong, FileChannel, Long> keyValue = channelMap.remove(fileHash);
        if (keyValue == null) {
            return;
        }
        try {
            FileChannel fileChannel = keyValue.getValue();
            fileChannel.close();
        } catch (Exception e) {
        }
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


    /**
     * 重建文件
     */
    @SneakyThrows
    public static void reCreateFile(String file) {
        File oldFile = new File(file);
        // 如果存在就删除
        Files.deleteIfExists(oldFile.toPath());
        // 创建父类目
        Files.createDirectories(oldFile.getParentFile().toPath());
        // 重建文件
        Files.createFile(oldFile.toPath());
    }

    public static boolean operabitilyCheck(String targetFile) {
        boolean match = channelMap.entrySet().stream().anyMatch(t -> t.getValue().getOther().equals(targetFile));
        // 如果match,说明当前文件不可用
        return !match;
    }
}