package com.murong.nets.util;

import com.google.common.primitives.Bytes;
import com.murong.nets.constant.AccessConstant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

/**
 * RSA 非对称算法加解密工具类
 *
 * @author murong
 */
@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RSAAlgorithmUtil {

    /**
     * 解密
     * 此处采用逆变算法,由公解密
     */
    public static String decrypt(String ciphertext) {
        //入参验证
        if (StringUtil.isBlank(ciphertext)) {
            throw new IllegalArgumentException("ciphertext/privateKey 都不允许为空");
        }
        try {
            //构建私钥
            PublicKey pKey = KeyFactory.getInstance("RSA").generatePublic(new PKCS8EncodedKeySpec(Base64.decodeBase64(AccessConstant.publicKey)));
            //根据私钥创建密码器实例
            Cipher instance = CryptoHelper.createCipher(Cipher.DECRYPT_MODE, pKey, "RSA/ECB/PKCS1Padding");
            //创建密码器实例计算密文，并转换成base64格式返回
            return new String(segmentUpdate(instance, Base64.decodeBase64(ciphertext), ((RSAPrivateKey) pKey).getModulus().bitLength() / 8), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(String.format("RAS解密：%s字符串解决发生异常!", ciphertext), e);
        }
    }

    /**
     * 从KeyPair中获取公钥Base64字符串
     *
     * @param keyPair keyPair实例
     * @return 公钥Base64字符串
     */
    public static String getPublicKeyString(KeyPair keyPair) {
        return Base64.encodeBase64String(keyPair.getPublic().getEncoded());
    }

    /**
     * 从KeyPair中获取私钥Base64字符串
     *
     * @param keyPair keyPair实例
     * @return 私钥Base64字符串
     */
    public static String getPrivateKeyString(KeyPair keyPair) {
        return Base64.encodeBase64String(keyPair.getPrivate().getEncoded());
    }

    /**
     * 生成指定长度的密钥对，默认生成1024长度的密钥对,综合安全、性能两个维度，
     * 在安全要求较高场景可指定为4096，一般情况可直接使用默认长度
     *
     * @param keySize 生成密钥对长度
     * @return 密钥对实例
     */
    public static KeyPair generatorKeyPair(Integer keySize) throws NoSuchAlgorithmException {
        int kSize;
        int minKeyLng = 1024;
        int maxKeyLng = 4096;
        if (keySize == null) {
            kSize = 1024;
        } else if (keySize >= minKeyLng && keySize <= maxKeyLng) {
            kSize = keySize;
        } else {
            throw new RuntimeException("密钥长度不符合要将，请在：1024/2048/3072/4094中选择一个作为密钥长度!");
        }

        //构建密钥生成器
        KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
        //初始化长度
        pairGenerator.initialize(kSize, new SecureRandom());

        //生成并返回密钥对
        return pairGenerator.genKeyPair();
    }

    /**
     * 根据指定长度分段计算
     *
     * @param cipherInstance 密码器实例
     * @param data           待处理字段数组
     * @param len            分段长度
     * @return 分段处理后结果汇总
     */
    private static byte[] segmentUpdate(Cipher cipherInstance, byte[] data, int len) throws IllegalBlockSizeException, BadPaddingException {
        //密文字节数组
        byte[] cipher = new byte[]{};

        //按指定长度分段计算密文
        for (int i = 0; i < data.length; i += len) {
            //分段获取密文字节数组
            cipherInstance.update(Arrays.copyOfRange(data, i, Math.min(i + len, data.length)));

            //合并分段结果
            cipher = Bytes.concat(cipher, cipherInstance.doFinal());
        }
        return cipher;
    }

}