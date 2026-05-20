package com.bookkeeping.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控工具类
 * 用于统计接口调用次数和平均耗时
 */
@Slf4j
@Component
public class PerformanceMonitor {

    private final Map<String, Metric> metrics = new ConcurrentHashMap<>();

    /**
     * 记录一次调用
     */
    public void record(String name, long executionTime) {
        Metric metric = metrics.computeIfAbsent(name, k -> new Metric());
        metric.record(executionTime);
    }

    /**
     * 获取统计信息
     */
    public Map<String, Metric> getMetrics() {
        return new ConcurrentHashMap<>(metrics);
    }

    /**
     * 重置统计
     */
    public void reset() {
        metrics.clear();
    }

    /**
     * 打印统计报告
     */
    public void printReport() {
        if (metrics.isEmpty()) {
            log.info("暂无性能统计数据");
            return;
        }

        log.info("========== 性能监控报告 ==========");
        metrics.forEach((name, metric) -> {
            log.info("{}: 调用次数={}, 平均耗时={}ms, 最大耗时={}ms, 最小耗时={}ms",
                    name,
                    metric.getCount(),
                    String.format("%.2f", metric.getAverage()),
                    metric.getMax(),
                    metric.getMin());
        });
        log.info("==================================");
    }

    /**
     * 指标数据
     */
    public static class Metric {
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private volatile long max = 0;
        private volatile long min = Long.MAX_VALUE;

        public synchronized void record(long executionTime) {
            count.incrementAndGet();
            totalTime.addAndGet(executionTime);

            if (executionTime > max) {
                max = executionTime;
            }
            if (executionTime < min) {
                min = executionTime;
            }
        }

        public long getCount() {
            return count.get();
        }

        public double getAverage() {
            long c = count.get();
            return c == 0 ? 0 : (double) totalTime.get() / c;
        }

        public long getMax() {
            return max;
        }

        public long getMin() {
            return min == Long.MAX_VALUE ? 0 : min;
        }
    }
}
