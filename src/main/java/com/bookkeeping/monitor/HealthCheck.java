package com.bookkeeping.monitor;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统健康检查组件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthCheck {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 执行系统健康检查
     */
    public Map<String, Object> check() {
        Map<String, Object> health = new HashMap<>();

        boolean allHealthy = true;

        // 检查数据库
        Map<String, Object> dbHealth = checkDatabase();
        health.put("database", dbHealth);
        if (!"UP".equals(dbHealth.get("status"))) {
            allHealthy = false;
        }

        // 检查Redis
        Map<String, Object> redisHealth = checkRedis();
        health.put("redis", redisHealth);
        if (!"UP".equals(redisHealth.get("status"))) {
            allHealthy = false;
        }

        // 检查线程池
        Map<String, Object> threadPoolHealth = checkThreadPool();
        health.put("threadPool", threadPoolHealth);

        health.put("status", allHealthy ? "UP" : "DOWN");
        health.put("timestamp", System.currentTimeMillis());

        return health;
    }

    /**
     * 检查数据库连接
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            if (dataSource instanceof DruidDataSource) {
                DruidDataSource druidDataSource = (DruidDataSource) dataSource;
                result.put("activeCount", druidDataSource.getActiveCount());
                result.put("poolingCount", druidDataSource.getPoolingCount());
                result.put("maxActive", druidDataSource.getMaxActive());
                result.put("minIdle", druidDataSource.getMinIdle());
            }

            try (Connection conn = dataSource.getConnection()) {
                result.put("status", "UP");
                result.put("message", "数据库连接正常");
            }
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            result.put("status", "DOWN");
            result.put("message", "数据库连接失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 检查Redis连接
     */
    private Map<String, Object> checkRedis() {
        Map<String, Object> result = new HashMap<>();
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            if ("PONG".equals(pong)) {
                result.put("status", "UP");
                result.put("message", "Redis连接正常");
            } else {
                result.put("status", "DOWN");
                result.put("message", "Redis连接异常");
            }
        } catch (Exception e) {
            log.error("Redis健康检查失败", e);
            result.put("status", "DOWN");
            result.put("message", "Redis连接失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 检查线程池状态
     */
    private Map<String, Object> checkThreadPool() {
        Map<String, Object> result = new HashMap<>();
        try {
            Runtime runtime = Runtime.getRuntime();
            int availableProcessors = runtime.availableProcessors();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            result.put("status", "UP");
            result.put("availableProcessors", availableProcessors);
            result.put("maxMemory", formatBytes(maxMemory));
            result.put("totalMemory", formatBytes(totalMemory));
            result.put("freeMemory", formatBytes(freeMemory));
            result.put("usedMemory", formatBytes(usedMemory));
            result.put("memoryUsagePercent", String.format("%.2f", (double) usedMemory / maxMemory * 100));
        } catch (Exception e) {
            log.error("线程池健康检查失败", e);
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }
        return result;
    }

    private String formatBytes(long bytes) {
        if (bytes < 0) {
            return "N/A";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
