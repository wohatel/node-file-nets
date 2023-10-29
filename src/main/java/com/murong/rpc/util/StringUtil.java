package com.murong.rpc.util;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 解析工具
 */
public class StringUtil {

    /**
     * 连接字符串
     *
     * @param spilt
     * @param sources
     * @return
     */
    public static String join(String spilt, String... sources) {
        if (sources == null || sources.length == 0) {
            return "";
        }
        List<String> strings = Arrays.asList(sources);
        return join(spilt, strings);
    }

    /**
     * 第一个非空字符串
     *
     * @param sources
     * @param sources
     * @return
     */
    public static String findFirstNoneBlank(String... sources) {
        if (sources == null || sources.length == 0) {
            return "";
        }
        for (int i = 0; i < sources.length; i++) {
            String source = sources[i];
            if (!StringUtil.isBlank(source)) {
                return source;
            }
        }
        return "";
    }

    /**
     * 连接字符串
     *
     * @param spilt
     * @param strs
     * @return
     */
    public static String join(String spilt, List<String> strs) {
        if (CollectionUtils.isEmpty(strs)) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (String st : strs) {
            stringBuffer.append(spilt).append(st);
        }
        return stringBuffer.substring(spilt.length());
    }

    /**
     * 连接字符串
     *
     * @param spilt
     * @param objs
     * @return
     */
    public static String joinObject(String spilt, Object... objs) {
        if (objs == null || objs.length == 0) {
            return "";
        }
        List<String> collect = Arrays.stream(objs).map(t -> t.toString()).collect(Collectors.toList());
        return join(spilt, collect);
    }

    /**
     * 判断字符串是否是空字符
     */
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        String trim = str.trim();
        if (trim.length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 返回后n位
     *
     * @param sourse
     * @param n
     * @return
     */
    public static String lastFromLastIndex(String sourse, int n) {
        if (sourse == null || n <= 0) {
            return null;
        }
        if (n >= sourse.length()) {
            return sourse;
        }
        int start = sourse.length() - n;
        return sourse.substring(start);
    }

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    /**
     * 驼峰转下划线
     *
     * @param str
     * @return
     */
    public static String humpToLine(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        if (sb.charAt(0) == '_') {
            return sb.substring(1);
        }
        return sb.toString();
    }

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    /**
     * 下划线转驼峰
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 比较相等
     */
    public static boolean equals(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    /**
     * 转string
     *
     * @param authHeader
     * @return
     */
    public static String toString(Object authHeader) {
        if (authHeader == null) {
            return "null";
        }
        return authHeader.toString();
    }

    /**
     * 获取之间的字符串
     * first
     * end
     */
    public static String findCharsInSection(String source, String start, String end) {
        if (source == null || source.length() == 0) {
            return "";
        }
        int i1 = source.indexOf(start);
        int i2 = source.indexOf(end, i1 + 1);
        if (i1 == -1 || i2 == -1) {
            return "";
        }
        return source.substring(i1 + 1, i2);
    }

    /**
     * 校验字符串
     */
    public static boolean verify(String source, String regx) {
        if (source == null) {
            return false;
        }
        return source.matches(regx);
    }

    /**
     * 校验英文数字下划线
     */
    public static boolean verifyEnHumNum(String source) {
        String regx = "^\\w+$";
        return verify(source, regx);
    }
}
