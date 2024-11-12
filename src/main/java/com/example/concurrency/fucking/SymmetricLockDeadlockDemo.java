package com.example.concurrency.fucking;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)

 * 这段代码展示了对称锁导致的死锁问题。在并发环境中，两个线程按相反的顺序请求相同的锁对象，
 *      可能会造成一个线程等待另一个线程释放锁，最终导致两个线程相互等待，形成死锁。

 * 一、 详细分析
 * - `lock1` 和 `lock2` 是两个共享的锁对象。
 * - `ConcurrencyCheckTask1` 和 `ConcurrencyCheckTask2` 是两个不同的任务，它们分别在 `thread1` 和 `thread2` 中运行。

 * 具体步骤如下：
 * 1. `ConcurrencyCheckTask1` 线程尝试按顺序获取 `lock1` 和 `lock2`。
 *    - 它首先获取 `lock1`，然后试图获取 `lock2`。
 * 2. 与此同时，`ConcurrencyCheckTask2` 线程按相反的顺序获取锁：
 *    - 它首先获取 `lock2`，然后试图获取 `lock1`。
 * 这就导致了死锁的情形：
 * - 假设 `thread1` 获得了 `lock1`，但此时 `thread2` 已获得了 `lock2`。
 * - `thread1` 在等待 `lock2` 释放，而 `thread2` 则在等待 `lock1` 释放。
 * - 这两个线程互相等待，导致死锁，程序将陷入停滞状态。

 * 二、 解决方案
 * 为了避免这种对称死锁，可以按照以下方法进行改进：
 * 1. 锁顺序的一致性：确保所有线程都按照相同的顺序获取锁。
 *      例如，让两个线程都先锁定 `lock1`，再锁定 `lock2`。这样可以避免两个线程相互等待。
 * 2. 使用更高级的锁机制：如 `ReentrantLock` 和 `tryLock`，可以实现超时和避免死锁。

 * 三、 总结
 * 这段代码展示了典型的对称锁死锁问题，通过确保所有线程按相同的顺序请求锁，可以有效避免这种死锁。
 */
public class SymmetricLockDeadlockDemo {
    static final Object lock1 = new Object();
    static final Object lock2 = new Object();

    public static void main(String[] args) throws Exception {
        Thread thread1 = new Thread(new ConcurrencyCheckTask1());
        thread1.start();
        Thread thread2 = new Thread(new ConcurrencyCheckTask2());
        thread2.start();
    }

    private static class ConcurrencyCheckTask1 implements Runnable {
        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            System.out.println("ConcurrencyCheckTask1 started!");
            while (true) {
                synchronized (lock1) {
                    synchronized (lock2) {
                        System.out.println("Hello1");
                    }
                }
            }
        }
    }

    private static class ConcurrencyCheckTask2 implements Runnable {
        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            System.out.println("ConcurrencyCheckTask2 started!");
            while (true) {
                synchronized (lock2) {
                    synchronized (lock1) {
                        System.out.println("Hello2");
                    }
                }
            }
        }
    }
}
