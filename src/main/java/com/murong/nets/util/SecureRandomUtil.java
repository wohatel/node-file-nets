package com.murong.nets.util;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.util.List;

/**
 * <p>
 * 随机获取
 * </p>
 *
 * @author yaochuang 2024/04/18 18:17
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecureRandomUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static int randomInt() {
        return SECURE_RANDOM.nextInt();
    }

    public static long randomLong() {
        return SECURE_RANDOM.nextLong();
    }

    public static int randomInt(int origin, int bound) {
        return SECURE_RANDOM.nextInt(origin, bound);
    }

    public static long randomLong(long origin, long bound) {
        return SECURE_RANDOM.nextLong(origin, bound);
    }

    public static <T> T randomOf(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(randomInt(0, list.size()));
    }

    /**
     * 生成随机的字母,数字
     *
     * @param len 生成的长度
     * @return String
     */
    public static String randomAlphabetic(int len) {
        // 48-57 表示 0-9
        // 65-90 表示 大小写字母(隔开7个字符)   映射为58-83
        // 97-122表示 小写字母(隔开49个字符)    映射为84-109
        // 48-57,58-83,84-109
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int i1 = randomInt(48, 110);
            char code;
            if (i1 < 58) {
                code = (char) i1;
            } else if (i1 < 84) {
                code = (char) (i1 + 7);
            } else {
                code = (char) (i1 + 13);
            }
            result.append(code);
        }
        return result.toString();
    }

    /**
     * 生成数字,字母,符号的随机数
     *
     * @param len 长度
     * @return String
     */
    public static String randomAlphabeticSymbol(int len) {
        // 33-126 表示数字,符号,英文字母
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int i1 = randomInt(33, 127);
            char code = (char) i1;
            result.append(code);
        }
        return result.toString();
    }

}
