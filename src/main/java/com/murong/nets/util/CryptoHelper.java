package com.murong.nets.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.function.ObjIntConsumer;

/**
 * description
 */
class CryptoHelper {

    private CryptoHelper() {
    }

    /**
     * 通过算法获取摘要信息
     *
     * @param message   源串
     * @param algorithm 算法名称
     * @return 摘要字节数组
     */
    static byte[] getStringDigest(String message, String algorithm) {
        //入参校验
        if (StringUtil.isBlank(message) || StringUtil.isBlank(algorithm)) {
            throw new IllegalArgumentException("message/algorithm 不允许为空");
        }

        MessageDigest digest = buildMessageDigest(algorithm);
        //将源转成字节数据进行处理
        digest.update(message.getBytes(StandardCharsets.UTF_8));

        //返回摘要字节数组
        return digest.digest();
    }

    /**
     * 获取文件摘要
     *
     * @param messageFile 文件
     * @param algorithm   算法名称
     * @return 摘要字节数组
     * @author niuniu 2024-04-24 16:41
     */
    static byte[] getFileDigest(File messageFile, String algorithm) {
        //入参校验
        if (Objects.isNull(messageFile) || !messageFile.exists()) {
            throw new IllegalArgumentException("messageFile 不允许为空或文件不存在");
        }

        //获取算法提供者实例
        MessageDigest digest = buildMessageDigest(algorithm);
        //分段处理文件内容
        CryptoHelper.readFileFragment(messageFile,
                (data, index) -> digest.update(data, 0, index));

        //返回摘要字节数组
        return digest.digest();
    }

    /**
     * 根据算法名称构建算法provider实例
     *
     * @param algorithm 算法名称
     * @return provider实例
     */
    static MessageDigest buildMessageDigest(String algorithm) {
        try {
            //构建provider实例
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(String.format("算法名：%s不存在", algorithm), e);
        }
    }

    /**
     * 读取文件片断的字节数组
     *
     * @param messageFile 文件
     * @param consumer    文件片断处理器
     */
    static void readFileFragment(File messageFile, ObjIntConsumer<byte[]> consumer) {
        try (BufferedInputStream inputStream =
                     new BufferedInputStream(new FileInputStream(messageFile))) {

            //读取文件内容
            int index;
            byte[] dataFragment = new byte[2048];
            while ((index = inputStream.read(dataFragment)) != -1) {
                //分段处理摘要信息
                consumer.accept(dataFragment, index);
            }
        } catch (Exception ex) {
            throw new RuntimeException(String.format("读取文件：%s发生异常!",
                    messageFile.getAbsolutePath()), ex);
        }
    }

    /**
     * MAC算法摘要实现
     *
     * @param key       密钥
     * @param algorithm 算法名称
     * @param message   源串
     * @return 摘要信息字节数组
     * @author niuniu 2024-01-03 16:59
     */
    static byte[] getStringDigest(String message, String algorithm, String... key) {
        //入参验证
        if (StringUtil.isBlank(message) || StringUtil.isBlank(algorithm)) {
            throw new IllegalArgumentException("message/algorithm 不允许为空");
        }

        try {
            //获取密钥的字节数组
            byte[] keyData = key.length == 0
                    ? CryptoHelper.generateKey(algorithm)
                    : key[0].getBytes(StandardCharsets.UTF_8);

            //生成密钥
            SecretKey secretKey = new SecretKeySpec(keyData, algorithm);
            //生成mac算法实例
            Mac instance = Mac.getInstance(secretKey.getAlgorithm());
            instance.init(secretKey);
            //计算源串摘要
            instance.update(message.getBytes(StandardCharsets.UTF_8));

            return instance.doFinal();
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    String.format("通过算法：%s获取摘要发生异常", algorithm), ex);
        }
    }

    /**
     * 根据算法生成密钥
     *
     * @param algorithm 算法名称
     * @return 密钥字节数组
     * @author niuniu 2024-01-03 16:43
     */
    static byte[] generateKey(String algorithm) throws NoSuchAlgorithmException {
        return KeyGenerator
                .getInstance(algorithm)
                .generateKey()
                .getEncoded();
    }

    /**
     * 实例化密码器(Cipher)实例
     *
     * @param model          加解密模式
     * @param key            密钥实例
     * @param transformation 算法及填充模式
     * @return Cipher实例
     * @author niuniu 2024-01-06 12:24
     */
    static Cipher createCipher(int model, Key key, String transformation)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        //获取算法实例
        Cipher cipherInstance = Cipher.getInstance(transformation);
        cipherInstance.init(model, key);

        return cipherInstance;
    }

    /**
     * 创建指定算法密钥实例
     *
     * @param secretKey 密钥串
     * @param algorithm 算法名称
     * @return 密钥实例
     * @author niuniu 2024-01-08 20:11
     */
    static Key createSecretKey(String secretKey, String algorithm) {
        return new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
    }
}
