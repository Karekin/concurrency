package com.example.concurrency.fucking;

/**
 * <a href="https://hllvm-group.iteye.com/group/topic/34932">请问R大 有没有什么工具可以查看正在运行的类的c/汇编代码</a>提到了<b>代码提升</b>的问题。
 *
 * @author Jerry Lee (oldratlee at gmail dot com)

 * 这段代码展示了一个典型的可见性问题，这是多线程并发编程中的常见问题之一。
 * 它与变量 `stop` 的值是否能被不同线程正确读取和写入有关。

 * ### 问题分析
 * 1. 线程间的变量可见性：
 *    - 代码中的 `stop` 变量被 `main` 线程设置为 `true`，以通知 `ConcurrencyCheckTask` 线程退出循环。
 *      然而在很多情况下，`ConcurrencyCheckTask` 线程并未感知到 `stop` 的改变，导致无限循环。
 * 2. 原因：CPU 缓存和编译优化：
 *    - 现代 CPU 使用缓存系统，线程会缓存共享变量的副本。
 *      `ConcurrencyCheckTask` 线程可能从缓存中读取 `stop` 的值，而不是直接读取最新值。
 *    - 同时，编译器和 CPU 可能会对代码进行优化，将 `stop` 的检查移动或提升，以提高程序性能。
 *      这些优化使得 `ConcurrencyCheckTask` 线程始终读取的可能是缓存中的 `false` 值，而非 `main` 线程更新的 `true` 值。
 * 3. 解决方案：`volatile` 关键字：
 *    - `volatile` 可以确保每次读取 `stop` 时都会从主存中读取最新值，并将对 `stop` 的修改立即同步到主存，从而保证线程的可见性。
 *    - 添加 `volatile` 后，`main` 线程的修改会被 `ConcurrencyCheckTask` 线程立即可见，从而可以正确地退出循环。

 * 代码解释
 * 在原始代码中，`stop` 字段未标记为 `volatile`，因此不同线程对该变量的修改无法立即被其他线程感知。
 * 添加 `volatile` 后，每次读取 `stop` 的值时都能获取到最新状态。

 * 总结
 * 此代码演示了没有 `volatile` 时，线程间对变量的修改可能不可见的情况。
 * 加上 `volatile` 是一种简单且有效的解决方案，
 * 它可以确保 `ConcurrencyCheckTask` 线程能够及时感知到 `stop` 的修改，避免线程无限循环。
 */
public class NoPublishDemo {
    boolean stop = false;

    public static void main(String[] args) throws Exception {
        // LoadMaker.makeLoad();

        NoPublishDemo demo = new NoPublishDemo();

        Thread thread = new Thread(demo.getConcurrencyCheckTask());
        thread.start();

        Thread.sleep(1000);
        System.out.println("Set stop to true in main!");
        demo.stop = true;
        System.out.println("Exit main.");
    }

    ConcurrencyCheckTask getConcurrencyCheckTask() {
        return new ConcurrencyCheckTask();
    }

    private class ConcurrencyCheckTask implements Runnable {
        @Override
        @SuppressWarnings({"WhileLoopSpinsOnField", "StatementWithEmptyBody"})
        public void run() {
            System.out.println("ConcurrencyCheckTask started!");
            // If the value of stop is visible in the main thread, the loop will exit.
            // On my dev machine, the loop almost never exits!
            // Simple and safe solution:
            //   add volatile to the `stop` field.
            while (!stop) {
            }
            System.out.println("ConcurrencyCheckTask stopped!");
        }
    }
}
