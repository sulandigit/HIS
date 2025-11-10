package com.neu.his.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 密钥管理服务
 * 负责加载、存储和管理JWT签名密钥
 */
@Service
public class KeyManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyManagementService.class);

    @Value("${jwt.key.mode:file}")
    private String keyMode;

    @Value("${jwt.key.rsaPrivateKeyPath:/config/keys/jwt-private.pem}")
    private String privateKeyPath;

    @Value("${jwt.key.rsaPublicKeyPath:/config/keys/jwt-public.pem}")
    private String publicKeyPath;

    @Value("${jwt.key.currentKeyVersion:v1}")
    private String currentKeyVersion;

    @Value("${jwt.secret:mySecret}")
    private String hmacSecret;

    // 存储多个版本的密钥对
    private Map<String, PrivateKey> privateKeys = new HashMap<>();
    private Map<String, PublicKey> publicKeys = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            if ("file".equals(keyMode)) {
                loadKeysFromFile();
            }
            LOGGER.info("密钥管理服务初始化成功，当前密钥版本: {}", currentKeyVersion);
        } catch (Exception e) {
            LOGGER.error("密钥加载失败", e);
            throw new RuntimeException("密钥管理服务初始化失败", e);
        }
    }

    /**
     * 从文件加载RSA密钥对
     */
    private void loadKeysFromFile() throws Exception {
        // 加载私钥
        PrivateKey privateKey = loadPrivateKeyFromFile(privateKeyPath);
        privateKeys.put(currentKeyVersion, privateKey);

        // 加载公钥
        PublicKey publicKey = loadPublicKeyFromFile(publicKeyPath);
        publicKeys.put(currentKeyVersion, publicKey);

        LOGGER.info("从文件加载密钥成功，版本: {}", currentKeyVersion);
    }

    /**
     * 从PEM文件加载私钥
     */
    private PrivateKey loadPrivateKeyFromFile(String filePath) throws Exception {
        try {
            String key = new String(Files.readAllBytes(Paths.get(filePath)));
            String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (IOException e) {
            LOGGER.error("读取私钥文件失败: {}", filePath, e);
            throw new Exception("私钥文件加载失败", e);
        }
    }

    /**
     * 从PEM文件加载公钥
     */
    private PublicKey loadPublicKeyFromFile(String filePath) throws Exception {
        try {
            String key = new String(Files.readAllBytes(Paths.get(filePath)));
            String publicKeyPEM = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (IOException e) {
            LOGGER.error("读取公钥文件失败: {}", filePath, e);
            throw new Exception("公钥文件加载失败", e);
        }
    }

    /**
     * 获取当前版本的私钥
     */
    public PrivateKey getCurrentPrivateKey() {
        return privateKeys.get(currentKeyVersion);
    }

    /**
     * 根据版本获取私钥
     */
    public PrivateKey getPrivateKey(String version) {
        return privateKeys.get(version);
    }

    /**
     * 获取当前版本的公钥
     */
    public PublicKey getCurrentPublicKey() {
        return publicKeys.get(currentKeyVersion);
    }

    /**
     * 根据版本获取公钥
     */
    public PublicKey getPublicKey(String version) {
        return publicKeys.get(version);
    }

    /**
     * 获取当前密钥版本
     */
    public String getCurrentKeyVersion() {
        return currentKeyVersion;
    }

    /**
     * 获取HMAC密钥（用于HS512算法兼容）
     */
    public String getHmacSecret() {
        return hmacSecret;
    }

    /**
     * 添加新的密钥版本（用于密钥轮换）
     */
    public void addKeyVersion(String version, PrivateKey privateKey, PublicKey publicKey) {
        privateKeys.put(version, privateKey);
        publicKeys.put(version, publicKey);
        LOGGER.info("添加新密钥版本: {}", version);
    }

    /**
     * 移除旧的密钥版本
     */
    public void removeKeyVersion(String version) {
        if (!version.equals(currentKeyVersion)) {
            privateKeys.remove(version);
            publicKeys.remove(version);
            LOGGER.info("移除密钥版本: {}", version);
        } else {
            LOGGER.warn("不能移除当前使用的密钥版本: {}", version);
        }
    }
}
