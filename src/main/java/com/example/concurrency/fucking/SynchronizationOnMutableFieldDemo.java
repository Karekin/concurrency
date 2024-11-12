package com.example.concurrency.fucking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)

 * 这段代码反映了对可变字段进行同步操作的并发问题，具体来说是失效更新（lost update）问题。
 *  代码尝试对一个可变的 List<Listener> 字段 listeners 进行同步操作，
 *      但由于使用的同步方式和对象的可变性，导致了并发不安全的问题。以下是关键点：

 * 一、 主要并发问题
 * 1. 可变字段同步的失效问题：
 *    - listeners 是一个 volatile 字段，但并没有解决线程安全问题。
 *    - 在 addListener 方法中，代码尝试通过同步块对 listeners 进行操作。
 *          然而，每次添加一个元素时，方法内部会创建一个新的 ArrayList（results），然后将其赋值给 listeners。
 *    - 即便在同步块中更新了 listeners，由于它是可变的，每次新赋值都会改变 listeners 的引用，
 *          从而导致并发访问的冲突，即两个线程可能会丢失更新（一个线程的更改会覆盖另一个线程的更改）。

 * 2. 并发环境下的竞态条件：
 *    - 线程 thread1 和 thread2 会同时调用 addListener 方法，
 *          各自会创建一个新的 ArrayList 并赋值给 listeners，最终导致一些更新丢失。
 *    - 即使使用了 volatile 关键字，这也无法解决问题，因为 volatile 仅保证可见性，不能确保操作的原子性。

 * 二、 代码中的解决方法提示
 * 代码注释中建议了更好的解决方案：使用线程安全的 CopyOnWriteArrayList（如代码中初始赋值所使用的）。
 *  但在 addListener 方法中，并没有直接使用它，而是创建了新的 ArrayList，
 *      这导致线程安全的 CopyOnWriteArrayList 失去了它的线程安全特性。

 * 三、 解决方案
 * 1. 直接使用 CopyOnWriteArrayList 的 add 方法：
 *    - CopyOnWriteArrayList 是一种线程安全的 List 实现，它允许多个线程安全地执行写操作。
 *          因此，我们可以直接调用 listeners.add(listener)，不需要再创建新的 ArrayList，也不需要同步块。

 * 四、 总结
 * 代码展示了在可变字段上同步操作的线程不安全问题。CopyOnWriteArrayList 是更合适的选择，
 *      可以避免失效更新和竞态条件的问题。CopyOnWriteArrayList 适合少量更新、多次读取的场景，
 *          因为每次写操作都会复制整个列表。在高频写入的情况下，它的性能可能不如其他线程安全的集合（例如 ConcurrentLinkedQueue）。
 */
public class SynchronizationOnMutableFieldDemo {
    static final int ADD_COUNT = 10000;

    static class Listener {
        // stub class
    }

    private volatile List<Listener> listeners = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws Exception {
        SynchronizationOnMutableFieldDemo demo = new SynchronizationOnMutableFieldDemo();

        Thread thread1 = new Thread(demo.getConcurrencyCheckTask());
        thread1.start();
        Thread thread2 = new Thread(demo.getConcurrencyCheckTask());
        thread2.start();

        thread1.join();
        thread2.join();

        int actualSize = demo.listeners.size();
        int expectedSize = ADD_COUNT * 2;
        if (actualSize != expectedSize) {
            // On my development machine, it's almost must occur!
            // Simple and safe solution:
            //   final List field and use concurrency-safe List, such as CopyOnWriteArrayList
            System.err.printf("Fuck! Lost update on mutable field! actual %s expected %s.%n", actualSize, expectedSize);
        } else {
            System.out.println("Emm... Got right answer!!");
        }
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void addListener(Listener listener) {
//        listeners.add(listener);

        synchronized (listeners) {
            List<Listener> results = new ArrayList<>(listeners);
            results.add(listener);
            listeners = results;
        }
    }

    ConcurrencyCheckTask getConcurrencyCheckTask() {
        return new ConcurrencyCheckTask();
    }

    private class ConcurrencyCheckTask implements Runnable {
        @Override
        public void run() {
            System.out.println("ConcurrencyCheckTask started!");
            for (int i = 0; i < ADD_COUNT; ++i) {
                addListener(new Listener());
            }
            System.out.println("ConcurrencyCheckTask stopped!");
        }
    }
}
