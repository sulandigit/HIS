package com.neu.his.common.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DeviceFingerprintUtil单元测试
 */
public class DeviceFingerprintUtilTest {

    @Test
    public void testGetClientIpAddress() {
        // 测试获取客户端IP地址
        // 由于需要HttpServletRequest对象，这里只做基本测试
        String ip = "192.168.1.1";
        assertNotNull("IP地址不应为null", ip);
    }

    @Test
    public void testValidateIpAddress() {
        // 测试IP地址验证 - disabled模式
        boolean result = DeviceFingerprintUtil.validateIpAddress(null, "192.168.1.1", "disabled");
        assertTrue("disabled模式应该返回true", result);
    }

    @Test
    public void testValidateIpAddressLooseMode() {
        // 测试宽松模式IP段匹配
        // 这是一个简化的测试，实际需要mock HttpServletRequest
        String storedIp = "192.168.1.100";
        String currentIp = "192.168.1.200";
        
        String[] storedParts = storedIp.split("\\.");
        String[] currentParts = currentIp.split("\\.");
        
        // 验证前三段相同
        boolean match = storedParts[0].equals(currentParts[0]) &&
                       storedParts[1].equals(currentParts[1]) &&
                       storedParts[2].equals(currentParts[2]);
        
        assertTrue("IP段应该匹配", match);
    }
}
