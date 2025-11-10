package com.neu.his.common.util;

import com.neu.his.common.service.KeyManagementService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JwtToken生成的工具类（增强版）
 * JWT token的格式：header.payload.signature
 * 支持RS256和HS512双算法
 * 新增功能：
 * 1. 支持RS256非对称加密算法
 * 2. 支持密钥版本管理
 * 3. 扩展Token Payload（jti, kid, deviceId, ipAddress等）
 * 4. 支持设备绑定和IP验证
 */
@Component
public class JwtTokenUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);
    
    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";
    private static final String CLAIM_KEY_JTI = "jti";
    private static final String CLAIM_KEY_DEVICE_ID = "deviceId";
    private static final String CLAIM_KEY_IP_ADDRESS = "ipAddress";
    private static final String CLAIM_KEY_SESSION_ID = "sessionId";
    
    @Autowired(required = false)
    private KeyManagementService keyManagementService;
    
    @Value("${jwt.secret:mySecret}")
    private String secret;
    
    @Value("${jwt.expiration:604800}")
    private Long expiration;
    
    @Value("${jwt.signature.algorithm:RS256}")
    private String algorithm;
    
    @Value("${jwt.security.deviceBinding.enabled:false}")
    private boolean deviceBindingEnabled;
    
    @Value("${jwt.security.ipValidation.enabled:false}")
    private boolean ipValidationEnabled;

    /**
     * 根据负载生成JWT的token（使用配置的算法）
     */
    private String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expirationDate = generateExpirationDate();
        
        // 添加标准claims
        claims.put("iat", now.getTime() / 1000);
        claims.put("nbf", now.getTime() / 1000);
        
        if ("RS256".equals(algorithm) && keyManagementService != null) {
            // 使用RS256算法
            return Jwts.builder()
                    .setHeaderParam("kid", keyManagementService.getCurrentKeyVersion())
                    .setClaims(claims)
                    .setExpiration(expirationDate)
                    .signWith(keyManagementService.getCurrentPrivateKey(), SignatureAlgorithm.RS256)
                    .compact();
        } else {
            // 使用HS512算法（兼容模式）
            Key key = Keys.hmacShaKeyFor(secret.getBytes());
            return Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(expirationDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        }
    }

    /**
     * 从token中获取JWT中的负载（支持多算法）
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            // 首先尝试解析header获取算法信息
            String[] parts = token.split("\\.");
            if (parts.length > 0) {
                // 尝试使用RS256解析
                if ("RS256".equals(algorithm) && keyManagementService != null) {
                    try {
                        claims = Jwts.parserBuilder()
                                .setSigningKey(keyManagementService.getCurrentPublicKey())
                                .build()
                                .parseClaimsJws(token)
                                .getBody();
                        return claims;
                    } catch (Exception e) {
                        LOGGER.debug("RS256验证失败，尝试HS512", e);
                    }
                }
                
                // 尝试使用HS512解析（兼容旧Token）
                Key key = Keys.hmacShaKeyFor(secret.getBytes());
                claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            }
        } catch (Exception e) {
            LOGGER.info("JWT格式验证失败:{}", token);
        }
        return claims;
    }

    /**
     * 生成token的过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    /**
     * 从token中获取登录用户名
     */
    public String getUserNameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }
    
    /**
     * 从token中获取JTI（Token唯一标识）
     */
    public String getJtiFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get(CLAIM_KEY_JTI, String.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从token中获取设备ID
     */
    public String getDeviceIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get(CLAIM_KEY_DEVICE_ID, String.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从token中获取IP地址
     */
    public String getIpAddressFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get(CLAIM_KEY_IP_ADDRESS, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证token是否还有效（增强版）
     *
     * @param token       客户端传入的token
     * @param userDetails 从数据库中查询出来的用户信息
     * @param request     HTTP请求对象（用于设备和IP验证）
     * @return 验证结果
     */
    public boolean validateToken(String token, UserDetails userDetails, HttpServletRequest request) {
        String username = getUserNameFromToken(token);
        
        // 基础验证：用户名和过期时间
        if (!username.equals(userDetails.getUsername()) || isTokenExpired(token)) {
            return false;
        }
        
        // 设备绑定验证
        if (deviceBindingEnabled && request != null) {
            String tokenDeviceId = getDeviceIdFromToken(token);
            if (tokenDeviceId != null) {
                String currentDeviceId = DeviceFingerprintUtil.generateDeviceFingerprint(request);
                if (!tokenDeviceId.equals(currentDeviceId)) {
                    LOGGER.warn("设备指纹不匹配 - username: {}, tokenDevice: {}, currentDevice: {}",
                               username, tokenDeviceId, currentDeviceId);
                    return false;
                }
            }
        }
        
        // IP地址验证
        if (ipValidationEnabled && request != null) {
            String tokenIp = getIpAddressFromToken(token);
            if (tokenIp != null) {
                String currentIp = DeviceFingerprintUtil.getClientIpAddress(request);
                if (!tokenIp.equals(currentIp)) {
                    LOGGER.warn("IP地址不匹配 - username: {}, tokenIp: {}, currentIp: {}",
                               username, tokenIp, currentIp);
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * 判断token是否已经失效
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        return expiredDate.before(new Date());
    }

    /**
     * 从token中获取过期时间
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 根据用户信息生成token（增强版）
     *
     * @param userDetails 用户信息
     * @param request     HTTP请求对象
     * @return Token字符串
     */
    public String generateToken(UserDetails userDetails, HttpServletRequest request) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        claims.put(CLAIM_KEY_JTI, UUID.randomUUID().toString());
        
        // 添加设备指纹
        if (deviceBindingEnabled && request != null) {
            String deviceId = DeviceFingerprintUtil.generateDeviceFingerprint(request);
            claims.put(CLAIM_KEY_DEVICE_ID, deviceId);
        }
        
        // 添加IP地址
        if (ipValidationEnabled && request != null) {
            String ipAddress = DeviceFingerprintUtil.getClientIpAddress(request);
            claims.put(CLAIM_KEY_IP_ADDRESS, ipAddress);
        }
        
        // 添加会话 ID
        claims.put(CLAIM_KEY_SESSION_ID, UUID.randomUUID().toString());
        
        return generateToken(claims);
    }

    /**
     * 根据用户信息生成token（兼容旧方法）
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, null);
    }

    /**
     * 验证token是否还有效（兼容旧方法）
     *
     * @param token       客户端传入的token
     * @param userDetails 从数据库中查询出来的用户信息
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        return validateToken(token, userDetails, null);
    }

    /**
     * 判断token是否可以被刷新
     */
    public boolean canRefresh(String token) {
        return !isTokenExpired(token);
    }

    /**
     * 刷新token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }
}
