package com.murong.nets.util;

import java.util.Arrays;
import java.util.List;

public class FileVerify {

    public static final List<String> list = Arrays.asList(" ", "\\", "\t", "\n", "\r", ",", "*");

    /**
     * 校验文件名是否包含特殊字符
     *
     * @param name
     * @return
     */
    public static boolean isFileNameOk(String name) {
        if (StringUtil.isBlank(name)) {
            return false;
        }
        long count = list.stream().filter(t -> name.contains(t)).count();
        return count == 0;
    }
}
