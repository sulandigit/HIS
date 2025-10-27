package com.neu.his.component;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.neu.his.bo.WebLog;
import com.neu.his.common.constant.LogConstants;
import com.neu.his.common.util.LogUtil;
import io.swagger.annotations.ApiOperation;
import net.logstash.logback.marker.Markers;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 统一日志处理切面
 * 功能:
 * 1. 记录所有Controller层的请求日志
 * 2. 统计接口性能
 * 3. 记录异常信息
 * 4. 敏感信息脱敏
 */
@Aspect
@Component
@Order(1)
public class WebLogAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogAspect.class);
    private ThreadLocal<Long> startTime = new ThreadLocal<>();
    
    // 敏感字段正则表达式
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("password|pwd|pass", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("phone|mobile|tel", Pattern.CASE_INSENSITIVE);
    private static final Pattern IDCARD_PATTERN = Pattern.compile("idcard|identitycard|idno", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("email|mail", Pattern.CASE_INSENSITIVE);

    @Pointcut("execution(public * com.neu.his.api.controller.*.*(..))")
    public void webLog() {
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        startTime.set(System.currentTimeMillis());
    }

    @AfterReturning(value = "webLog()", returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable {
        // 可以在此处添加返回值的额外处理
    }
    
    /**
     * 异常通知
     */
    @AfterThrowing(pointcut = "webLog()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            
            // 构建异常日志
            Map<String, Object> errorLog = new HashMap<>();
            errorLog.put("type", LogConstants.LogType.EXCEPTION);
            errorLog.put("url", request.getRequestURL().toString());
            errorLog.put("method", request.getMethod());
            errorLog.put("ip", getIpAddress(request));
            errorLog.put("className", joinPoint.getSignature().getDeclaringTypeName());
            errorLog.put("methodName", joinPoint.getSignature().getName());
            errorLog.put("exception", e.getClass().getName());
            errorLog.put("message", e.getMessage());
            errorLog.put("stackTrace", LogUtil.formatException(e));
            
            LOGGER.error("[异常日志] {}", JSONUtil.toJsonStr(errorLog), e);
        } catch (Exception ex) {
            LOGGER.error("记录异常日志失败", ex);
        } finally {
            startTime.remove();
        }
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = null;
        HttpServletRequest request = null;
        WebLog webLog = null;
        
        try {
            // 获取当前请求对象
            attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return joinPoint.proceed();
            }
            request = attributes.getRequest();
            
            // 记录请求信息
            webLog = WebLog.builder().build();
            
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
            
            // 获取操作描述
            if (method.isAnnotationPresent(ApiOperation.class)) {
                ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
                webLog.setDescription(apiOperation.value());
            }
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 计算耗时
            long endTime = System.currentTimeMillis();
            Long startTimeValue = startTime.get();
            int spendTime = startTimeValue != null ? (int) (endTime - startTimeValue) : 0;
            
            // 填充日志信息
            String urlStr = request.getRequestURL().toString();
            webLog.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));
            webLog.setIp(getIpAddress(request));
            webLog.setMethod(request.getMethod());
            webLog.setParameter(desensitizeParameter(getParameter(method, joinPoint.getArgs())));
            webLog.setResult(desensitizeResult(result));
            webLog.setSpendTime(spendTime);
            webLog.setStartTime(startTimeValue);
            webLog.setUri(request.getRequestURI());
            webLog.setUrl(urlStr);
            webLog.setOs(getOperatingSystem(request));
            webLog.setBrowser(getBrowser(request));
            
            // 构建结构化日志
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("type", LogConstants.LogType.ACCESS);
            logMap.put("description", webLog.getDescription());
            logMap.put("url", webLog.getUrl());
            logMap.put("uri", webLog.getUri());
            logMap.put("method", webLog.getMethod());
            logMap.put("ip", webLog.getIp());
            logMap.put("parameter", webLog.getParameter());
            logMap.put("spendTime", webLog.getSpendTime());
            logMap.put("os", webLog.getOs());
            logMap.put("browser", webLog.getBrowser());
            
            // 根据耗时选择日志级别
            if (spendTime > LogConstants.PerformanceThreshold.SLOW_API) {
                logMap.put("performanceWarning", "接口响应时间过长");
                LOGGER.warn(Markers.appendEntries(logMap), "[慢接口] {}", JSONUtil.toJsonStr(webLog));
            } else {
                LOGGER.info(Markers.appendEntries(logMap), JSONUtil.toJsonStr(webLog));
            }
            
            return result;
            
        } catch (Throwable e) {
            // 异常会被@AfterThrowing捕获
            throw e;
        } finally {
            startTime.remove();
        }
    }

    /**
     * 根据方法和传入的参数获取请求参数
     */
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if (!StringUtils.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if (argList.size() == 0) {
            return null;
        } else if (argList.size() == 1) {
            return argList.get(0);
        } else {
            return argList;
        }
    }
    
    /**
     * 脱敏请求参数
     */
    private Object desensitizeParameter(Object parameter) {
        if (parameter == null) {
            return null;
        }
        
        try {
            String jsonStr = JSONUtil.toJsonStr(parameter);
            jsonStr = desensitizeJson(jsonStr);
            return JSONUtil.parse(jsonStr);
        } catch (Exception e) {
            LOGGER.debug("参数脱敏失败", e);
            return parameter;
        }
    }
    
    /**
     * 脱敏返回结果
     */
    private Object desensitizeResult(Object result) {
        if (result == null) {
            return null;
        }
        
        try {
            String jsonStr = JSONUtil.toJsonStr(result);
            // 只对部分字段进行脱敏，完整结果可能太大
            if (jsonStr.length() > 10000) {
                return "[结果过大，已省略]";  
            }
            return JSONUtil.parse(desensitizeJson(jsonStr));
        } catch (Exception e) {
            LOGGER.debug("结果脱敏失败", e);
            return "[无法序列化]";  
        }
    }
    
    /**
     * 对JSON字符串中的敏感信息进行脱敏
     */
    private String desensitizeJson(String json) {
        if (json == null) {
            return null;
        }
        
        // 脱敏密码相关字段
        json = json.replaceAll("(\"(?:password|pwd|pass)\"\\s*:\\s*\")([^\"]*)(\"", "$1******$3");
        
        // 可以添加更多脱敏规则
        // 注意: 这里使用简单的正则替换，对于复杂场景建议使用JSON解析器
        
        return json;
    }
    
    /**
     * 获取真实IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于多级代理，取第一个非unknown的IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }
    
    /**
     * 获取操作系统信息
     */
    private String getOperatingSystem(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.toLowerCase().contains("windows")) {
            return "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            return "MacOS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            return "Linux";
        } else if (userAgent.toLowerCase().contains("android")) {
            return "Android";
        } else if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
            return "iOS";
        }
        return "Unknown";
    }
    
    /**
     * 获取浏览器信息
     */
    private String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("Edge")) {
            return "Edge";
        } else if (userAgent.contains("Chrome")) {
            return "Chrome";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            return "Safari";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "IE";
        }
        return "Unknown";
    }
}
