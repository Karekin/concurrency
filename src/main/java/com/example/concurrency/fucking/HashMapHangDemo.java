package com.example.concurrency.fucking;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see <a href="http://coolshell.cn/articles/9606.html">Infinite loop of Java HashMap</a> by <a href="http://github.com/haoel">@haoel</a>
 *
 * 这段代码展示了在多线程环境中对 非线程安全的 `HashMap` 进行并发操作所导致的无限循环或挂起问题。
 * 这是 `HashMap` 在并发写操作下容易出现的经典问题之一。

 * 一、问题分析
 * 1. HashMap 的并发问题：
 *    - `HashMap` 不是线程安全的。在多线程环境中同时对 `HashMap` 进行读写或写入操作时，
 *          可能会导致数据结构损坏（如链表形成循环），从而引发无限循环或程序挂起。
 *    - 当一个线程在执行 `put` 操作时（即插入新元素），`HashMap` 可能会触发扩容（resize）。
 *          扩容过程中涉及重新计算元素位置并重新分配存储空间。如果多个线程同时触发 `put` 操作，就会引发数据竞争，
 *          导致数据结构的状态不一致，甚至形成环形链表，导致 `get` 操作进入死循环。
 * 2. 死循环或挂起的原因：
 *    - `HashMap` 的 `put` 和 `get` 操作并不是线程安全的。在 `HashMap` 进行扩容时，
 *          内部的链表可能会因多线程的并发插入操作而变成循环链表。
 *    - 一旦形成循环链表，任何遍历 `HashMap` 的操作（如 `get`）都会进入死循环，导致程序挂起。
 * 3. 代码具体行为：
 *    - 代码在 `main` 方法中启动两个线程，线程中不断向 `holder` 这个共享 `HashMap` 中插入随机键值对，
 *          同时 `main` 线程不断地从 `holder` 中进行 `get` 操作。
 *    - 如果 `HashMap` 在并发操作下出现了挂起问题，则会阻塞在 `get` 操作中，无法输出预期的 "Got key …" 消息。

 *  二、解决方案
 * 1. 使用线程安全的集合：
 *    - 可以使用 `ConcurrentHashMap` 替代 `HashMap`，它是线程安全的，并且可以在并发环境中正确工作。
 * 2. 同步锁：
 *    - 另一种方案是对 `HashMap` 的所有访问操作加锁，确保每次只有一个线程能够访问 `HashMap`。
 *          但这可能导致性能下降，尤其在频繁操作时。
 * 三、示例修改
 * 将 `HashMap` 替换为 `ConcurrentHashMap` 以避免并发问题：

 * 四、总结
 * 此代码反映了 `HashMap` 在多线程环境中的非线程安全性，在并发操作下可能导致死循环或程序挂起。
 */
public class HashMapHangDemo {
    final Map<Integer, Object> holder = new HashMap<>(1); // 设置较小的初始容量

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        HashMapHangDemo demo = new HashMapHangDemo();
        for (int i = 0; i < 100; i++) {
            demo.holder.put(i, null);
        }

        // 启动更多线程
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(demo.getConcurrencyCheckTask());
            thread.start();
        }


        System.out.println("Start get in main!");
        for (int i = 0; ; ++i) {
            for (int j = 0; j < 10000; ++j) {
                demo.holder.get(j);

                // If the hashmap occurs hang problem, the following output will not appear again.
                // On my dev machine, this problem is easily observed in the first round.
                System.out.printf("Got key %s in round %s%n", j, i);
            }
        }
    }

    ConcurrencyTask getConcurrencyCheckTask() {
        return new ConcurrencyTask();
    }

    private class ConcurrencyTask implements Runnable {
        private final Random random = new Random();

        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            System.out.println("Add loop started in task!");
            while (true) {
                holder.put(random.nextInt() % (1024 * 1024 * 100), null);
            }
        }
    }
}
