package com.neu.his.common.util;

import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * 设备指纹工具类
 * 用于生成和验证设备唯一标识
 */
public class DeviceFingerprintUtil {

    /**
     * 生成设备指纹
     * 基于User-Agent、IP地址等信息生成唯一标识
     *
     * @param request HTTP请求对象
     * @return 设备指纹字符串
     */
    public static String generateDeviceFingerprint(HttpServletRequest request) {
        StringBuilder fingerprintBuilder = new StringBuilder();

        // 获取User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            fingerprintBuilder.append(userAgent);
        }

        // 获取Accept-Language
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null) {
            fingerprintBuilder.append(acceptLanguage);
        }

        // 获取Accept-Encoding
        String acceptEncoding = request.getHeader("Accept-Encoding");
        if (acceptEncoding != null) {
            fingerprintBuilder.append(acceptEncoding);
        }

        // 生成MD5哈希作为设备指纹
        return DigestUtils.md5DigestAsHex(fingerprintBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证设备指纹是否匹配
     *
     * @param request            当前请求
     * @param storedFingerprint  存储的设备指纹
     * @return 是否匹配
     */
    public static boolean validateDeviceFingerprint(HttpServletRequest request, String storedFingerprint) {
        if (storedFingerprint == null || storedFingerprint.isEmpty()) {
            return true; // 如果没有存储的指纹，则不进行验证
        }
        String currentFingerprint = generateDeviceFingerprint(request);
        return storedFingerprint.equals(currentFingerprint);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求对象
     * @return IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 验证IP地址是否匹配
     *
     * @param request    当前请求
     * @param storedIp   存储的IP地址
     * @param mode       验证模式：disabled/loose/strict
     * @return 是否匹配
     */
    public static boolean validateIpAddress(HttpServletRequest request, String storedIp, String mode) {
        if ("disabled".equals(mode) || storedIp == null || storedIp.isEmpty()) {
            return true;
        }

        String currentIp = getClientIpAddress(request);

        if ("strict".equals(mode)) {
            // 严格模式：精确匹配
            return storedIp.equals(currentIp);
        } else if ("loose".equals(mode)) {
            // 宽松模式：IP段匹配（前三段）
            if (currentIp == null) {
                return false;
            }
            String[] storedParts = storedIp.split("\\.");
            String[] currentParts = currentIp.split("\\.");
            if (storedParts.length >= 3 && currentParts.length >= 3) {
                return storedParts[0].equals(currentParts[0]) &&
                       storedParts[1].equals(currentParts[1]) &&
                       storedParts[2].equals(currentParts[2]);
            }
            return false;
        }

        return true;
    }
}
