package com.example.concurrency;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import com.example.concurrency.features.synchronizedcase.SynchronizedExample;

import static org.springframework.test.util.AssertionErrors.assertEquals;

public class SynchronizedTest {

    @Test
    public void testSynchronizedMethodsOnSameObject() throws InterruptedException {
        // 创建同一个对象实例，演示 synchronized 方法锁定同一个对象的行为
        SynchronizedExample.B obj = new SynchronizedExample.B();
        Thread thread1 = new Thread(obj::a);
        Thread thread2 = new Thread(obj::b);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
        // 由于是演示互斥，不做断言
    }

    @Test
    public void testSynchronizedMethodsOnDifferentObjects() throws InterruptedException {
        // 创建不同对象实例，演示不同锁对象的行为
        SynchronizedExample.B obj1 = new SynchronizedExample.B();
        SynchronizedExample.B obj2 = new SynchronizedExample.B();
        Thread thread1 = new Thread(obj1::a);
        Thread thread2 = new Thread(obj2::b);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // 由于不同对象不会互相阻塞，正常情况下应并发执行，不做断言
    }

    @Test
    public void testSynchronizedBlocksWithDifferentLocks() throws InterruptedException {
        // 创建同一个对象，但锁定不同的 synchronized 块（lockA 和 lockB）
        SynchronizedExample.C obj = new SynchronizedExample.C();
        Thread thread1 = new Thread(obj::a);
        Thread thread2 = new Thread(obj::b);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // 由于 a() 和 b() 使用不同的锁对象，正常情况下应并发执行，不做断言
    }

    @Test
    public void testSafeCalcConcurrency() throws InterruptedException {
        // 创建 SafeCalc 对象，并发调用 addOne 方法，最终计数应为 1000
        SynchronizedExample.SafeCalc safeCalc = new SynchronizedExample.SafeCalc();
        List<Thread> threads = new ArrayList<>(100);

        for (int j = 0; j < 100; j++) {
            Thread t = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    safeCalc.addOne();
                }
            });
            threads.add(t);
        }

        // 启动所有线程
        for (Thread t : threads) {
            t.start();
        }

        // 等待所有线程执行完成
        for (Thread t : threads) {
            t.join();
        }

        // 验证最终结果是否为 1000
        assertEquals("Final value in SafeCalc should be 1000", 1000, safeCalc.get());
    }

    @Test
    public void testSynchronizedStaticMethods() throws InterruptedException {
        // 测试两个线程分别调用静态 synchronized 方法 a() 和 b()
        Thread thread1 = new Thread(SynchronizedExample.D::a, "Thread-1");
        Thread thread2 = new Thread(SynchronizedExample.D::b, "Thread-2");

        // 启动线程
        thread1.start();
        thread2.start();

        // 等待线程完成
        thread1.join();
        thread2.join();

        // 输出观察顺序，两个方法互斥执行
    }
}

