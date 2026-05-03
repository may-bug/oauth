package org.codelin.oauth.oauth.identity.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * 加密服务 - RSA+AES混合加密
 */
@Slf4j
@Service
public class CryptoService {

    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * 生成RSA密钥对
     *
     * @param keySize 密钥长度（1024, 2048, 4096）
     * @return 包含公钥和私钥的Map
     */
    public Map<String, String> generateKeyPair(int keySize) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            return Map.of("publicKey", publicKey, "privateKey", privateKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    /**
     * RSA+AES混合加密
     * <p>
     * 流程：
     * 1. 生成随机AES密钥
     * 2. 用AES密钥加密数据
     * 3. 用RSA公钥加密AES密钥
     * 4. 返回加密后的AES密钥 + 加密后的数据
     *
     * @param data      原始数据
     * @param publicKey RSA公钥（Base64）
     * @return 加密结果（Base64编码）
     */
    public String encrypt(String data, String publicKey) {
        try {
            // 1. 生成随机AES密钥
            KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGen.init(AES_KEY_SIZE);
            SecretKey aesKey = keyGen.generateKey();

            // 2. 用AES加密数据
            byte[] iv = generateIv();
            byte[] encryptedData = aesEncrypt(data.getBytes(StandardCharsets.UTF_8), aesKey, iv);

            // 3. 用RSA公钥加密AES密钥
            PublicKey rsaPublicKey = parsePublicKey(publicKey);
            byte[] encryptedAesKey = rsaEncrypt(aesKey.getEncoded(), rsaPublicKey);

            // 4. 组合结果: encryptedAesKey + iv + encryptedData
            byte[] result = new byte[4 + encryptedAesKey.length + iv.length + encryptedData.length];
            // 写入AES密钥长度
            result[0] = (byte) (encryptedAesKey.length >> 24);
            result[1] = (byte) (encryptedAesKey.length >> 16);
            result[2] = (byte) (encryptedAesKey.length >> 8);
            result[3] = (byte) encryptedAesKey.length;
            System.arraycopy(encryptedAesKey, 0, result, 4, encryptedAesKey.length);
            System.arraycopy(iv, 0, result, 4 + encryptedAesKey.length, iv.length);
            System.arraycopy(encryptedData, 0, result, 4 + encryptedAesKey.length + iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * RSA+AES混合解密
     *
     * @param encryptedData 加密数据（Base64）
     * @param privateKey    RSA私钥（Base64）
     * @return 原始数据
     */
    public String decrypt(String encryptedData, String privateKey) {
        try {
            byte[] data = Base64.getDecoder().decode(encryptedData);

            // 1. 解析加密的AES密钥长度
            int aesKeyLength = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) |
                    ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);

            // 2. 提取加密的AES密钥、IV和加密数据
            byte[] encryptedAesKey = new byte[aesKeyLength];
            System.arraycopy(data, 4, encryptedAesKey, 0, aesKeyLength);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(data, 4 + aesKeyLength, iv, 0, GCM_IV_LENGTH);

            byte[] cipherData = new byte[data.length - 4 - aesKeyLength - GCM_IV_LENGTH];
            System.arraycopy(data, 4 + aesKeyLength + GCM_IV_LENGTH, cipherData, 0, cipherData.length);

            // 3. 用RSA私钥解密AES密钥
            PrivateKey rsaPrivateKey = parsePrivateKey(privateKey);
            byte[] aesKeyBytes = rsaDecrypt(encryptedAesKey, rsaPrivateKey);
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, AES_ALGORITHM);

            // 4. 用AES密钥解密数据
            byte[] decryptedData = aesDecrypt(cipherData, aesKey, iv);

            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage(), e);
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * RSA加密
     */
    private byte[] rsaEncrypt(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepSpec);
        return cipher.doFinal(data);
    }

    /**
     * RSA解密
     */
    private byte[] rsaDecrypt(byte[] data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        OAEPParameterSpec oaepSpec = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepSpec);
        return cipher.doFinal(data);
    }

    /**
     * AES-GCM加密
     */
    private byte[] aesEncrypt(byte[] data, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        return cipher.doFinal(data);
    }

    /**
     * AES-GCM解密
     */
    private byte[] aesDecrypt(byte[] data, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(data);
    }

    /**
     * 生成随机IV
     */
    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * 解析公钥
     */
    private PublicKey parsePublicKey(String publicKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(spec);
    }

    /**
     * 解析私钥
     */
    private PrivateKey parsePrivateKey(String privateKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(spec);
    }
}
