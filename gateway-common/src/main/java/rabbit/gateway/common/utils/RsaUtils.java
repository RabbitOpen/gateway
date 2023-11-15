package rabbit.gateway.common.utils;

import rabbit.gateway.common.exception.GateWayException;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaUtils {

    /**
     * 签名算法
     */
    private String signAlgorithm = "SHA256withRSA";

    /**
     * 摘要算法
     */
    private String encodeAlgorithm = "SHA-256";

    private static final RsaUtils inst = new RsaUtils();

    private RsaUtils() {}

    /**
     * 私钥签名
     *
     * @param plainData
     * @param privateKey
     * @return
     */
    public static byte[] signWithPrivateKey(String plainData, PrivateKey privateKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance(inst.encodeAlgorithm);
            digest.update(plainData.getBytes());
            Signature signature = Signature.getInstance(inst.signAlgorithm);
            signature.initSign(privateKey);
            signature.update(digest.digest());
            return signature.sign();
        } catch (Exception e) {
            throw new GateWayException(e);
        }
    }

    /**
     * 公钥验签
     *
     * @param input
     * @param plainData
     * @param publicKey
     * @return
     */
    public static boolean verifyWithPublicKey(byte[] input, String plainData, PublicKey publicKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance(inst.encodeAlgorithm);
            digest.update(plainData.getBytes());
            Signature signature = Signature.getInstance(inst.signAlgorithm);
            signature.initVerify(publicKey);
            signature.update(digest.digest());
            return signature.verify(input);
        } catch (Exception e) {
            throw new GateWayException(e);
        }
    }

    /**
     * 16进制公钥转java对象
     *
     * @param hexKeyStr 16进制公钥
     * @return
     */
    public static PublicKey loadPublicKeyFromString(String hexKeyStr) {
        try {
            byte[] bytes = HexUtils.toBytes(hexKeyStr);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (Exception e) {
            throw new GateWayException(e);
        }
    }

    /**
     * 16进制私钥转java对象
     *
     * @param hexKeyStr 16进制私钥
     * @return
     */
    public static PrivateKey loadPrivateKeyFromString(String hexKeyStr) {
        try {
            byte[] bytes = HexUtils.toBytes(hexKeyStr);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePrivate(spec);
        } catch (Exception e) {
            throw new GateWayException(e);
        }
    }

    /**
     * 公钥加密
     *
     * @param plainData
     * @param publicKey
     * @return
     */
    public static byte[] encryptWithPublicKey(String plainData, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(plainData.getBytes());
        } catch (Exception e) {
            throw new GateWayException(e);
        }
    }

    /**
     * 私钥解密
     * @param encryptedBytes
     * @param key
     * @return
     */
    public static byte[] decryptWithPrivateKey(byte[] encryptedBytes, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            throw new GateWayException(e);
        }
    }

    /**
     * 生成密钥对
     * @param keySize
     * @return
     */
    public static KeyPair generateKeyPair(int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize, new SecureRandom());
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new GateWayException(e);
        }
    }
}
