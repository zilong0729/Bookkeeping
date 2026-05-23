package com.bookkeeping.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 异步任务执行器 - 支持重试机制
 */
@Slf4j
@Component
public class AsyncTaskExecutor {

    /**
     * 异步执行任务（无重试）
     */
    @Async
    public void execute(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("异步任务执行失败", e);
        }
    }

    /**
     * 异步执行任务（带重试）
     */
    @Async
    public void executeWithRetry(Runnable task, int maxRetries) {
        executeWithRetry(task, maxRetries, 1000);
    }

    /**
     * 异步执行任务（带重试和延迟）
     */
    @Async
    public void executeWithRetry(Runnable task, int maxRetries, long delayMs) {
        int attempts = 0;
        while (attempts <= maxRetries) {
            try {
                task.run();
                if (attempts > 0) {
                    log.info("异步任务重试成功: attempts={}", attempts);
                }
                return;
            } catch (Exception e) {
                attempts++;
                log.warn("异步任务执行失败（尝试 {}/{}）: {}", attempts, maxRetries, e.getMessage());
                if (attempts <= maxRetries) {
                    try {
                        Thread.sleep(delayMs * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("任务重试等待被中断", ie);
                        return;
                    }
                }
            }
        }
        log.error("异步任务执行失败，已重试 {} 次", maxRetries);
    }

    /**
     * 异步执行任务并返回结果（带重试）
     */
    public <T> CompletableFuture<T> executeWithRetryAndResult(Supplier<T> task, int maxRetries) {
        return executeWithRetryAndResult(task, maxRetries, 1000);
    }

    /**
     * 异步执行任务并返回结果（带重试和延迟）
     */
    public <T> CompletableFuture<T> executeWithRetryAndResult(Supplier<T> task, int maxRetries, long delayMs) {
        return CompletableFuture.supplyAsync(() -> {
            int attempts = 0;
            while (attempts <= maxRetries) {
                try {
                    T result = task.get();
                    if (attempts > 0) {
                        log.info("异步任务重试成功: attempts={}", attempts);
                    }
                    return result;
                } catch (Exception e) {
                    attempts++;
                    log.warn("异步任务执行失败（尝试 {}/{}）: {}", attempts, maxRetries, e.getMessage());
                    if (attempts <= maxRetries) {
                        try {
                            Thread.sleep(delayMs * attempts);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("任务重试等待被中断", ie);
                            throw new RuntimeException("任务执行被中断", ie);
                        }
                    }
                }
            }
            throw new RuntimeException("异步任务执行失败，已重试 " + maxRetries + " 次");
        });
    }
}
