package com.neu.his.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 * 提供统一的日志输出方法和敏感信息脱敏功能
 * 
 * @author HIS Team
 * @date 2025
 */
public class LogUtil {
    
    /**
     * 获取Logger实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * 脱敏手机号
     * 格式: 138****8888
     */
    public static String desensitizePhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
    
    /**
     * 脱敏身份证号
     * 格式: 110***********1234
     */
    public static String desensitizeIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.replaceAll("(\\d{3})\\d+(\\d{4})", "$1***********$2");
    }
    
    /**
     * 脱敏姓名
     * 格式: 张*、李**、欧阳**
     */
    public static String desensitizeName(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < name.length(); i++) {
            sb.append("*");
        }
        return sb.toString();
    }
    
    /**
     * 脱敏密码
     * 完全隐藏
     */
    public static String desensitizePassword(String password) {
        if (password == null || password.length() == 0) {
            return password;
        }
        return "******";
    }
    
    /**
     * 脱敏银行卡号
     * 格式: 6222****1234
     */
    public static String desensitizeBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) {
            return bankCard;
        }
        return bankCard.replaceAll("(\\d{4})\\d+(\\d{4})", "$1****$2");
    }
    
    /**
     * 脱敏邮箱
     * 格式: t***@example.com
     */
    public static String desensitizeEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String prefix = parts[0];
        if (prefix.length() <= 1) {
            return email;
        }
        return prefix.charAt(0) + "***@" + parts[1];
    }
    
    /**
     * 通用脱敏方法
     * 只显示前后各n个字符
     */
    public static String desensitize(String str, int showLength) {
        if (str == null || str.length() <= showLength * 2) {
            return str;
        }
        String prefix = str.substring(0, showLength);
        String suffix = str.substring(str.length() - showLength);
        return prefix + "***" + suffix;
    }
    
    /**
     * 记录业务日志（带用户信息）
     */
    public static void logBusiness(Logger logger, String username, String operation, String detail) {
        logger.info("[业务日志] 用户:{} 操作:{} 详情:{}", username, operation, detail);
    }
    
    /**
     * 记录性能日志
     */
    public static void logPerformance(Logger logger, String method, long timeMs) {
        if (timeMs > 3000) {
            logger.warn("[性能日志] 方法:{} 耗时:{}ms (超过3秒)", method, timeMs);
        } else if (timeMs > 1000) {
            logger.info("[性能日志] 方法:{} 耗时:{}ms", method, timeMs);
        } else {
            logger.debug("[性能日志] 方法:{} 耗时:{}ms", method, timeMs);
        }
    }
    
    /**
     * 记录数据库操作日志
     */
    public static void logDatabase(Logger logger, String operation, String table, String detail) {
        logger.debug("[数据库日志] 操作:{} 表:{} 详情:{}", operation, table, detail);
    }
    
    /**
     * 记录外部接口调用日志
     */
    public static void logExternalApi(Logger logger, String apiName, String url, long timeMs, boolean success) {
        if (success) {
            logger.info("[外部接口] 接口:{} URL:{} 耗时:{}ms 状态:成功", apiName, url, timeMs);
        } else {
            logger.error("[外部接口] 接口:{} URL:{} 耗时:{}ms 状态:失败", apiName, url, timeMs);
        }
    }
    
    /**
     * 格式化异常信息
     */
    public static String formatException(Throwable e) {
        if (e == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage());
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            // 只记录前5层堆栈
            int limit = Math.min(5, stackTrace.length);
            for (int i = 0; i < limit; i++) {
                sb.append("\n\tat ").append(stackTrace[i].toString());
            }
        }
        return sb.toString();
    }
}
