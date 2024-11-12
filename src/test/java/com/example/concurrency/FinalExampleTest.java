package com.example.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.concurrency.features.finalcase.FinalExample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class FinalExampleTest {

    private static final int NUM_THREADS = 100;
    private static ExecutorService executor;

    @BeforeEach
    public void setup() {
        executor = Executors.newFixedThreadPool(NUM_THREADS);
        FinalExample.Global.example = null;
    }

    @RepeatedTest(20) // 重复多次测试以增加问题暴露的可能性
    public void testFinalExampleConcurrency() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(NUM_THREADS);

        // 创建多个线程并发执行 writer 和 reader 操作
        for (int i = 0; i < NUM_THREADS; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程准备好
                    FinalExample.writer(); // 执行构造函数
                    FinalExample.reader(); // 读取构造后的值
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown(); // 线程完成
                }
            });
        }

        // 开始所有线程
        startLatch.countDown();
        doneLatch.await();

        // 验证结果
        FinalExample example = FinalExample.Global.example;
        assertNotNull(example, "Global.example should not be null");

        // 如果重排序发生，可能看到 x 或 y 的默认值
        assertEquals(3, example.x, "Expected final variable x to be 3");
        assertEquals(4, example.y, "Expected variable y to be 4");
    }
}

