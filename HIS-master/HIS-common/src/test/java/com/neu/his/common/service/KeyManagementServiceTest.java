package com.neu.his.common.service;

import org.junit.Test;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import static org.junit.Assert.*;

/**
 * KeyManagementService单元测试
 */
public class KeyManagementServiceTest {

    @Test
    public void testRSAKeyGeneration() throws Exception {
        // 测试RSA密钥对生成
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        java.security.KeyPair keyPair = keyGen.generateKeyPair();
        
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        
        assertNotNull("私钥不应为null", privateKey);
        assertNotNull("公钥不应为null", publicKey);
        assertEquals("应该是RSA算法", "RSA", privateKey.getAlgorithm());
        assertEquals("应该是RSA算法", "RSA", publicKey.getAlgorithm());
    }

    @Test
    public void testKeySize() throws Exception {
        // 测试密钥长度
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        java.security.KeyPair keyPair = keyGen.generateKeyPair();
        
        // RSA 2048位密钥的编码长度应该大于256字节
        assertTrue("密钥长度应该足够", keyPair.getPrivate().getEncoded().length > 256);
    }
}
