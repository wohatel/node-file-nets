package com.murong.nets.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArrayUtil {

    public static byte[] toBytes(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return null;
        }
        byte[] array = byteBuffer.array();
        int limit = byteBuffer.limit();
        return clone(array, limit);
    }

    public static byte[] clone(byte[] bytes, int len) {
        byte[] newBytes = new byte[len];
        System.arraycopy(bytes, 0, newBytes, 0, len);
        return newBytes;
    }
}
