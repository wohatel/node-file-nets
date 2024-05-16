package com.murong.nets.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * description
 *
 * @author yaochuang 2024/05/16 15:41
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandUtil {

    @SneakyThrows
    @SuppressWarnings("all")
    public static void exec(String shellFile, String execDir, String logToFile, long execSecondLimit) {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("/bin/bash", shellFile);
                pb.redirectErrorStream(true); // 将错误输出合并到标准输出
                if (!StringUtil.isBlank(execDir)) {
                    pb.directory(new File(execDir));
                }
                // 启动进程
                Process process = pb.start();
                if (!StringUtil.isBlank(logToFile)) {
                    try (FilesTool filesTool = FilesTool.getInstance().buildWriter(new File(logToFile));) {
                        // 读取进程输出
                        InputStream inputStream = process.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            filesTool.append(line + "\n");
                        }
                    }
                }
                if (execSecondLimit > 0) {
                    // 等待命令执行完毕
                    process.waitFor(execSecondLimit, TimeUnit.SECONDS);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
