package com.neu.his.common.cache;

import com.neu.his.common.cache.stats.CacheStatistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

/**
 * 多级缓存测试类
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiLevelCacheTest {
    
    @Autowired
    private MultiLevelCacheManager cacheManager;
    
    @Autowired
    private CacheStatistics cacheStatistics;
    
    @Test
    public void testMultiLevelCache() {
        // 创建缓存键
        CacheKey cacheKey = CacheKey.of("test", "user", "1001");
        
        // 第一次获取,会从数据源加载
        String value1 = cacheManager.get(cacheKey, CacheLevel.L1_AND_L2, () -> {
            System.out.println("从数据源加载数据...");
            return "用户数据-1001";
        }, 300);
        
        System.out.println("第一次获取: " + value1);
        
        // 第二次获取,会从L1缓存读取
        String value2 = cacheManager.get(cacheKey, CacheLevel.L1_AND_L2, () -> {
            System.out.println("从数据源加载数据...");
            return "用户数据-1001";
        }, 300);
        
        System.out.println("第二次获取: " + value2);
        
        // 删除缓存
        cacheManager.evict(cacheKey, CacheLevel.L1_AND_L2);
        
        // 第三次获取,会重新从数据源加载
        String value3 = cacheManager.get(cacheKey, CacheLevel.L1_AND_L2, () -> {
            System.out.println("从数据源加载数据...");
            return "用户数据-1001";
        }, 300);
        
        System.out.println("第三次获取: " + value3);
    }
    
    @Test
    public void testCacheStatistics() {
        // 执行一些缓存操作
        for (int i = 0; i < 10; i++) {
            CacheKey key = CacheKey.of("test", "item", String.valueOf(i));
            cacheManager.get(key, CacheLevel.L1_AND_L2, () -> "数据-" + i, 300);
        }
        
        // 重复访问,测试命中率
        for (int i = 0; i < 10; i++) {
            CacheKey key = CacheKey.of("test", "item", String.valueOf(i));
            cacheManager.get(key, CacheLevel.L1_AND_L2, () -> "数据-" + i, 300);
        }
        
        // 打印统计信息
        cacheStatistics.printAllCacheStats();
        
        // 获取统计数据
        Map<String, CacheStatistics.CacheStatsInfo> stats = cacheStatistics.getAllCacheStats();
        System.out.println("\n统计数据:");
        stats.forEach((name, info) -> {
            System.out.println(name + " - 命中率: " + (info.getHitRate() * 100) + "%");
        });
    }
    
    @Test
    public void testDifferentCacheLevels() {
        CacheKey key = CacheKey.of("test", "level", "1");
        
        // 测试L1_ONLY
        System.out.println("\n===== 测试 L1_ONLY =====");
        String l1Only = cacheManager.get(key, CacheLevel.L1_ONLY, () -> {
            System.out.println("从数据源加载(L1_ONLY)...");
            return "L1数据";
        }, 300);
        System.out.println("结果: " + l1Only);
        
        // 测试L2_ONLY
        System.out.println("\n===== 测试 L2_ONLY =====");
        String l2Only = cacheManager.get(key, CacheLevel.L2_ONLY, () -> {
            System.out.println("从数据源加载(L2_ONLY)...");
            return "L2数据";
        }, 300);
        System.out.println("结果: " + l2Only);
        
        // 测试L1_AND_L2
        System.out.println("\n===== 测试 L1_AND_L2 =====");
        String both = cacheManager.get(key, CacheLevel.L1_AND_L2, () -> {
            System.out.println("从数据源加载(L1_AND_L2)...");
            return "多级数据";
        }, 300);
        System.out.println("结果: " + both);
    }
}
