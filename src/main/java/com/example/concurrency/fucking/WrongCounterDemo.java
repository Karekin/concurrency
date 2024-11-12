package com.example.concurrency.fucking;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)

 * 这个代码反映了并发计数不准确的问题。当多个线程同时更新同一个计数器时，如果不使用线程安全的操作，
 *      最终的计数结果可能会不符合预期。这种情况在未使用适当的同步机制时容易发生，表现为"计数丢失"。

 * 一、并发问题的原因

 * 让我们看看代码中具体可能出现的问题：
 * 1. 未同步的递增操作：如果 counter 使用的是普通的 int 类型（甚至是 volatile int），
 *      每个线程执行 ++counter 操作时，实际上会经历如下步骤：
 *    - 读取 counter 的值。
 *    - 递增该值。
 *    - 将递增后的值写回 counter。
 *    在并发环境中，多个线程会并发执行这些操作。如果两个线程同时读取了相同的 counter 值并执行了递增操作，
 *      它们都会将递增后的相同值写回 counter，导致一次计数丢失。即便是 volatile 也无法保证这三个操作的原子性。

 * 2. 解决方案：AtomicInteger：代码中通过 AtomicInteger 来解决这个问题。
 *      AtomicInteger 提供了一种线程安全的方式来执行原子递增操作。
 *          它的 incrementAndGet() 方法可以确保在多线程环境下递增操作的完整性，避免计数丢失。

 * 二、 代码解释

 * - 该程序启动了两个线程，并让它们各自对 counter 执行 INC_COUNT 次的递增。
 * - 最终，main 线程检查 counter 的值是否等于预期值 INC_COUNT * 2。
 * - 如果未使用 AtomicInteger 而使用普通的 int，可能会导致 counter 的最终值小于预期值，因为会有计数丢失。

 * 三、 结论
 * 这段代码展示了计数丢失的并发问题。即使使用 volatile 修饰 counter，
 *      也无法保证递增操作的原子性。AtomicInteger 是一种简单而有效的解决方案，
 *          可以确保 incrementAndGet() 操作的线程安全性，避免计数丢失。
 */
public class WrongCounterDemo {
    private static final int INC_COUNT = 100000000;

//    private volatile int counter = 0;
    private final AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        WrongCounterDemo demo = new WrongCounterDemo();

        System.out.println("Start task thread!");
        Thread thread1 = new Thread(demo.getConcurrencyCheckTask());
        thread1.start();
        Thread thread2 = new Thread(demo.getConcurrencyCheckTask());
        thread2.start();

        thread1.join();
        thread2.join();
//        int actualCounter = demo.counter;
        int actualCounter = demo.counter.get();
        int expectedCount = INC_COUNT * 2;
        if (actualCounter != expectedCount) {
            // Even if volatile is added to the counter field,
            // On my dev machine, it's almost must occur!
            // Simple and safe solution:
            //   use AtomicInteger
            System.err.printf("Fuck! Got wrong count!! actual %s, expected: %s.%n", actualCounter, expectedCount);
        } else {
            System.out.println("Wow... Got right count!");
        }
    }

    ConcurrencyCheckTask getConcurrencyCheckTask() {
        return new ConcurrencyCheckTask();
    }

    private class ConcurrencyCheckTask implements Runnable {
        @Override
        @SuppressWarnings("NonAtomicOperationOnVolatileField")
        public void run() {
            for (int i = 0; i < INC_COUNT; ++i) {
//                ++counter;
                counter.incrementAndGet();
            }
        }
    }
}
