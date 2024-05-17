package com.murong.nets.constant;

import java.time.LocalDateTime;

/**
 * 日志记录
 *
 * @author yaochuang 2024/05/17 13:42
 */
public record LogRecord(String ip, String osName, String browser, String method, String params,
                        LocalDateTime localDateTime) {
}
