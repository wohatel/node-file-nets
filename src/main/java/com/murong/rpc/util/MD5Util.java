package com.murong.rpc.util;



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
public class MD5Util {

    /**
     * @Title: getMD5
     * @Description: 根据字符串生成md5
     * @author: logic
     * @date: 2021/3/23 16:42
     * @params:
     * @return:
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
            e.printStackTrace();
        }
        return result;
    }

}
