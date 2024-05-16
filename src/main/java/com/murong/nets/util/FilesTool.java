package com.murong.nets.util;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件；文档（file的复数）工具
 */
public class FilesTool implements AutoCloseable {
    /**
     * 获取
     */
    public static FilesTool getInstance() {
        /**
         * [医]缓冲的读者
         */
        return new FilesTool();
    }

    /**
     * 读缓冲
     */
    private BufferedReader bufferedReader;
    /**
     * [医]缓冲的作家
     */
    private BufferedWriter bufferedWriter;

    /**
     * 建立reader
     */
    @SneakyThrows
    public FilesTool buildReader(File file) {
        if (file == null) {
            throw new RpcException("file is null");
        }
        if (!file.exists()) {
            throw new RpcException("file is not exists");
        }

        FileReader fileReader = new FileReader(file);
        this.bufferedReader = new BufferedReader(fileReader);
        return this;
    }

    /**
     * 建立writer
     */
    @SneakyThrows
    public FilesTool buildWriter(File file) {
        if (file == null) {
            throw new RpcException("file is null");
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file);
        this.bufferedWriter = new BufferedWriter(fileWriter);
        return this;
    }

    /**
     * 读取string 文件
     *
     * @param content
     * @return
     */
    public FilesTool buildStringReader(String content) {
        StringReader stringReader = new StringReader(content);
        this.bufferedReader = new BufferedReader(stringReader);
        return this;
    }

    /**
     * 读取文件行
     *
     * @return
     */
    @SneakyThrows
    public String readLine() {
        return bufferedReader.readLine();
    }

    /**
     * 向文件中写入字符
     *
     * @param str
     */
    @SneakyThrows
    public FilesTool append(String str) {
        bufferedWriter.append(str);
        return this;
    }

    /**
     * 关闭文件流
     */
    @Override
    public void close() throws IOException {
        if (this.bufferedWriter != null) {
            this.bufferedWriter.flush();
            this.bufferedWriter.close();
        }
        if (this.bufferedReader != null) {
            this.bufferedReader.close();
        }
    }


    /**
     * 创建文件
     *
     * @param file 文件全路径
     */
    @SneakyThrows
    public static void createFile(String file) {
        Path path = Paths.get(file);
        Files.createDirectories(path.getParent());
        Files.createFile(path);
    }

    @SneakyThrows
    public static void createFile(File file) {
        Path path = file.toPath();
        Files.createDirectories(path.getParent());
        Files.createFile(path);
    }

}
