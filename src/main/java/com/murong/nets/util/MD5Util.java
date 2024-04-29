package com.murong.nets.util;


import lombok.extern.java.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * CopyRright(c)2017-2020 Logic  <p>
 * Package com.elib.edu.common.util
 * FileName  MD5Util <p>
 * Describe  <p>
 * author   logic <p>
 * version  v1.0 <p>
 * CreateDate  2021/3/23 16:36 <p>
 */
@Log
public class MD5Util {

    /**
     * @param : content 内容
     * @return: String hash
     */
    public static String getMD5(CharSequence content) {
        if (content == null || content.length() == 0) {
            return null;
        }
        String result = null;
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(content.toString().getBytes());
            byte[] bytes = md.digest();
            for (byte b : bytes) {
                String str = Integer.toHexString(b & 0xFF);
                if (str.length() == 1) {
                    sb.append("0");
                }
                sb.append(str);
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warning(e.getMessage());
        }
        return result;
    }

}
