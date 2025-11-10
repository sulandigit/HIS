package com.neu.his.cloud.api.app.component;


import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
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

/**
 * 统一日志处理切面
 * <p>
 * 该切面用于拦截Controller层的所有请求，记录请求的详细信息，包括：
 * - 请求URL、方法类型、参数
 * - 请求耗时
 * - 返回结果
 * 日志通过Logstash传入Elasticsearch进行存储和分析
 * </p>
 *
 * @author HIS System
 * @version 1.0
 */
@Aspect
@Component
@Order(1)  // 设置切面执行顺序为1，确保在其他切面之前执行
public class WebLogAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogAspect.class);
    
    /**
     * 使用ThreadLocal存储每个请求的开始时间
     * 避免多线程环境下的数据混乱
     */
    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    /**
     * 定义切点：拦截com.neu.his.cloud.api.app.controller包下所有类的所有public方法
     * <p>
     * 切点表达式说明：
     * - execution: 执行表达式
     * - public: 方法修饰符
     * - *: 任意返回值类型
     * - com.neu.his.cloud.api.app.controller.*.*: 包路径.类名.方法名
     * - (..): 任意参数
     * </p>
     */
    @Pointcut("execution(public * com.neu.his.cloud.api.app.controller.*.*(..))")
    // @Pointcut("execution(public * com.neu.his.cloud.zuul.controller..*.*(..))")
    public void webLog() {
    }

    /**
     * 前置通知：在目标方法执行前记录开始时间
     * <p>
     * 该方法在Controller方法执行之前被调用，用于记录请求开始的时间戳
     * </p>
     *
     * @param joinPoint 连接点，包含了目标方法的信息
     * @throws Throwable 可能抛出的异常
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 记录请求开始时间，用于后续计算请求耗时
        startTime.set(System.currentTimeMillis());
    }

    /**
     * 后置返回通知：在目标方法正常返回后执行
     * <p>
     * 注意：该方法当前为空实现，主要日志记录逻辑在@Around通知中完成
     * </p>
     *
     * @param ret 目标方法的返回值
     * @throws Throwable 可能抛出的异常
     */
    @AfterReturning(value = "webLog()", returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable {
        // 预留方法，可用于特殊的返回值处理逻辑
    }

    /**
     * 环绕通知：在目标方法执行前后进行处理，记录完整的请求日志
     * <p>
     * 该方法是日志记录的核心，完成以下工作：
     * 1. 获取HTTP请求信息
     * 2. 执行目标方法
     * 3. 收集请求参数、返回结果、耗时等信息
     * 4. 将日志结构化后输出到Logstash
     * </p>
     *
     * @param joinPoint 连接点，可以执行目标方法
     * @return 目标方法的返回值
     * @throws Throwable 目标方法可能抛出的异常
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        
        // 创建WebLog对象，用于记录请求信息(通过logstash传入elasticsearch)
        WebLog webLog = new WebLog();
        
        // 执行目标方法，获取返回结果
        Object result = joinPoint.proceed();
        
        // 获取方法签名信息
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        // 获取方法上的@ApiOperation注解，提取接口描述信息
        if (method.isAnnotationPresent(ApiOperation.class)) {
            ApiOperation log = method.getAnnotation(ApiOperation.class);
            webLog.setDescription(log.value());
        }
        // 计算请求耗时
        long endTime = System.currentTimeMillis();
        
        // 组装WebLog对象，记录详细的请求信息
        String urlStr = request.getRequestURL().toString();
        webLog.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));  // 基础路径
        webLog.setIp(request.getRemoteUser());  // 远程用户IP
        webLog.setMethod(request.getMethod());  // HTTP方法类型(GET/POST等)
        webLog.setParameter(getParameter(method, joinPoint.getArgs()));  // 请求参数
        webLog.setResult(result);  // 返回结果
        webLog.setSpendTime((int) (endTime - startTime.get()));  // 请求耗时(毫秒)
        webLog.setStartTime(startTime.get());  // 请求开始时间
        webLog.setUri(request.getRequestURI());  // 请求URI
        webLog.setUrl(request.getRequestURL().toString());  // 完整请求URL
        // 构建结构化日志Map，用于Logstash字段映射
        Map<String,Object> logMap = new HashMap<>();
        logMap.put("url", webLog.getUrl());
        logMap.put("method", webLog.getMethod());
        logMap.put("parameter", webLog.getParameter());
        logMap.put("spendTime", webLog.getSpendTime());
        logMap.put("description", webLog.getDescription());
        
        // 使用Logstash的Markers功能，将日志以结构化方式输出到Elasticsearch
        // Markers.appendEntries会将logMap中的字段作为单独的字段存储，方便检索和分析
        LOGGER.info(Markers.appendEntries(logMap), JSONUtil.parse(webLog).toString());
        
        // 返回目标方法的执行结果
        return result;
    }

    /**
     * 根据方法和传入的参数获取请求参数
     * <p>
     * 该方法解析方法参数上的注解(@RequestBody、@RequestParam)，
     * 提取实际的请求参数值，用于日志记录
     * </p>
     *
     * @param method 目标方法
     * @param args 方法的实际参数值数组
     * @return 请求参数对象，可能是单个对象、Map或List
     */
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        
        // 遍历所有参数，提取带有@RequestBody或@RequestParam注解的参数
        for (int i = 0; i < parameters.length; i++) {
            // 处理@RequestBody注解的参数（通常是POST请求的JSON body）
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            
            // 处理@RequestParam注解的参数（URL参数或表单参数）
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                // 获取参数名，优先使用@RequestParam中指定的value，否则使用参数本身的名称
                String key = parameters[i].getName();
                if (!StringUtils.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        // 根据参数数量返回不同格式的结果
        if (argList.size() == 0) {
            return null;  // 无参数
        } else if (argList.size() == 1) {
            return argList.get(0);  // 单个参数，直接返回该参数
        } else {
            return argList;  // 多个参数，返回参数列表
        }
    }
}
