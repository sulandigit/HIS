package com.neu.his.filter;

import com.neu.his.common.constant.LogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * 日志追踪过滤器
 * 为每个请求生成唯一的TraceId,用于日志追踪
 * 
 * @author HIS Team
 * @date 2025
 */
@Component
@Order(1)
public class LogTraceFilter implements Filter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LogTraceFilter.class);
    
    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";
    private static final String REQUEST_URI = "requestUri";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("LogTraceFilter 初始化");
    }
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                        FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        
        try {
            // 生成或获取TraceId
            String traceId = request.getHeader(TRACE_ID);
            if (traceId == null || traceId.isEmpty()) {
                traceId = generateTraceId();
            }
            
            // 将TraceId放入MDC,这样日志中就会自动包含TraceId
            MDC.put(TRACE_ID, traceId);
            MDC.put(REQUEST_URI, request.getRequestURI());
            
            // 如果有用户信息,也可以放入MDC
            // String userId = getUserIdFromRequest(request);
            // if (userId != null) {
            //     MDC.put(USER_ID, userId);
            // }
            
            // 继续执行过滤器链
            filterChain.doFilter(servletRequest, servletResponse);
            
        } finally {
            // 清除MDC中的数据,防止内存泄漏
            MDC.clear();
        }
    }
    
    @Override
    public void destroy() {
        LOGGER.info("LogTraceFilter 销毁");
    }
    
    /**
     * 生成TraceId
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 从请求中获取用户ID(示例方法)
     */
    private String getUserIdFromRequest(HttpServletRequest request) {
        // 这里可以从token或session中获取用户ID
        // 具体实现根据项目的认证方式而定
        return null;
    }
}
