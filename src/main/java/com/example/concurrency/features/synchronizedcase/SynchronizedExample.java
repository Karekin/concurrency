package com.example.concurrency.features.synchronizedcase;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 * Synchronized 实现原理 基于操作系统 Mutex Lock (互斥锁)实现，
 *      所以每次获取和释放都会有用户态和内核态的切换，成本高，jdk1.5之前性能差
 * JVM 通过 ACC_SYNCHRONIZED 标识一个方法是否为同步方法,
 *      而代码块则通过 monitorenter 和 monitorexit 指令操作 monitor 对象
 ** @author zed
 * @since 2019-06-13 11:47 AM
 */
import java.util.ArrayList;
import java.util.List;

/**
 * 一类题目：请问这两个线程能并发执行吗？需要明确 synchronized 的作用域！
 */
public class SynchronizedExample {

    /**
     * 示例类 A 演示 synchronized 关键字的不同使用方式
     */
    public static class A {
        /**
         * synchronized 修饰非静态方法，锁对象为当前实例对象 this
         */
        synchronized void get() {
            System.out.println("Non-static synchronized method: locked by instance (this)");
        }

        /**
         * synchronized 修饰静态方法，锁对象为当前类的 Class 对象 (A.class)
         */
        synchronized static void set() {
            System.out.println("Static synchronized method: locked by Class object (A.class)");
        }

        /**
         * 使用 synchronized 块，锁对象为自定义的锁 obj
         */
        private final Object obj = new Object();
        void put() {
            synchronized (obj) {
                System.out.println("Synchronized block: locked by custom object (obj)");
            }
        }
    }

    /**
     * 示例类 B 演示两个 synchronized 方法锁定同一个对象
     * 当两个线程试图调用同一对象的不同 synchronized 方法时，只有一个线程可以获得对象锁，因此无法并发执行。
     */
    public static class B {
        /*
            这道题主要考察的是 Java 中的同步机制，特别是 synchronized 关键字的作用和线程并发执行的条件。

            1. synchronized 关键字的作用
            在 Java 中，synchronized 用于确保在多线程环境下，只有一个线程能够访问被 synchronized 修饰的代码块或方法。这里要明确两点：
            - synchronized 方法：当一个线程访问一个对象的 synchronized 方法时，
                该对象会被加锁，其他线程不能再访问该对象的其他 synchronized 方法，直到该线程释放锁。
            - 对象锁：synchronized 方法锁的是调用该方法的对象，而不是类本身。
                如果多个线程调用的是同一个对象的 synchronized 方法，那么这些线程是互斥的，因为它们会争夺同一个对象锁。

            2. 能否并发执行的条件
            代码中 A.a() 和 A.b() 都是 synchronized 方法，而它们属于同一个对象 A。
                在这种情况下，两个线程会争夺同一把对象锁，因此不能并发执行。具体来说：
            - 当第一个线程调用 A.a() 时，它会获取对象 A 的锁，这时其他线程（比如调用 A.b() 的线程）必须等待锁被释放，才能执行 A.b()。
            - 直到调用 A.a() 的线程执行完毕，释放了对象锁，第二个线程才能继续执行 A.b()。

            因此，这两个线程 不能并发执行，它们是互斥的。

            3. 并发执行的情况
            如果你希望 A.a() 和 A.b() 能够并发执行，可以从以下几种方式考虑：
            1) 不同对象的锁：如果你在两个不同的对象上调用 a() 和 b()，那么它们使用的锁不同，可以并发执行。

            2) 同步块而非同步方法：你也可以通过使用 synchronized 块控制锁的粒度，而不是锁住整个方法。
         */
        public synchronized void a() {
            System.out.println("Executing method a");
            try {
                Thread.sleep(1000); // 模拟耗时操作
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Finished method a");
        }

        public synchronized void b() {
            System.out.println("Executing method b");
            try {
                Thread.sleep(1000); // 模拟耗时操作
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Finished method b");
        }
    }

    /**
     * 示例类 C 演示局部 synchronized 块，使用不同的锁对象实现并发
     * 由于 a 和 b 使用不同的锁对象 lockA 和 lockB，即便是同一个对象 obj，两个方法也能并发执行。
     */
    public static class C {
        private final Object lockA = new Object();
        private final Object lockB = new Object();

        public void a() {
            synchronized (lockA) {
                System.out.println("Executing method a with lockA");
                try {
                    Thread.sleep(1000); // 模拟耗时操作
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Finished method a");
            }
        }

        public void b() {
            synchronized (lockB) {
                System.out.println("Executing method b with lockB");
                try {
                    Thread.sleep(1000); // 模拟耗时操作
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Finished method b");
            }
        }
    }

    /*
        在 Java 中，静态 synchronized 方法的锁定对象是类的 Class 对象，而不是实例对象。这意味着：
        当一个线程调用 B.a() 时，它会获取 B.class 的锁，其他线程必须等待 B.class 锁被释放才能进入 B 类的任何静态 synchronized 方法。
        因此，在一个类的两个 synchronized 静态方法上加锁时，同样会发生互斥行为。
        即：即使调用的是不同的静态方法（如 a() 和 b()），只要它们都是 synchronized 的，线程将会互斥等待。
     */
    public static class D {
        public synchronized static void a() {
            System.out.println(Thread.currentThread().getName() + " - Executing method a");
            try {
                Thread.sleep(1000); // 模拟耗时操作
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " - Finished method a");
        }

        public synchronized static void b() {
            System.out.println(Thread.currentThread().getName() + " - Executing method b");
            try {
                Thread.sleep(1000); // 模拟耗时操作
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " - Finished method b");
        }
    }

    /**
     * SafeCalc 类演示 synchronized 方法实现线程安全的自增操作
     */
    public static class SafeCalc {
        private long value = 0L;

        /**
         * synchronized get 方法用于读取 value，确保原子性
         */
        public synchronized long get() {
            return value;
        }

        /**
         * synchronized addOne 方法用于增加 value 的值，确保原子性
         */
        public synchronized void addOne() {
            value += 1;
        }
    }
}


