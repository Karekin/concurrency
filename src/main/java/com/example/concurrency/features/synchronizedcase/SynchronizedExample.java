package com.example.concurrency.features.synchronizedcase;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 * Synchronized 实现原理 基于操作系统 Mutex Lock (互斥锁)实现，
 *      所以每次获取和释放都会有用户态和内核态的切换，成本高，jdk1.5之前性能差
 * JVM 通过 ACC_SYNCHRONIZED 标识一个方法是否为同步方法,
 *      而代码块则通过 monitorenter 和 monitorexit 指令操作 monitor 对象
 *
 *
 *
 ** @author zed
 * @since 2019-06-13 11:47 AM
 */
public class SynchronizedExample {

    static class A {
        /**
         * 修饰非静态方法 锁对象为当前类的实例对象 this
         */
        synchronized void get() {
        }

        /**
         * 修饰静态方法 锁对象为当前类的Class对象 Class X
         */
        synchronized static void set() {
        }

        /**
         * 修饰代码块
         */
        Object obj = new Object();
        void put() {
            synchronized(obj) {
            }
        }
    }

    static class B {
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

    static class C {
        private final Object lockA = new Object();
        private final Object lockB = new Object();

        public void a() {
            synchronized (lockA) {
                System.out.println("Executing method a");
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
                System.out.println("Executing method b");
                try {
                    Thread.sleep(1000); // 模拟耗时操作
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Finished method b");
            }
        }
    }

    /**
     * 利用Synchronized 实现原子操作
     */
    static class SafeCalc {
        long value = 0L;
        synchronized long get() {
            return value;
        }
        synchronized void addOne() {
            value += 1;
        }
    }

    public static void main(String[] args) {
        // 一、执行 B：两个 synchronized 方法锁定同一个对象，无法并发执行
        B obj = new B();

        // 启动两个线程，分别调用 a() 和 b() 方法
        new Thread(obj::a).start();
        new Thread(obj::b).start();

        // 二、使用不同对象锁的情况
        B obj1 = new B();
        B obj2 = new B();

        new Thread(obj1::a).start();
        new Thread(obj2::b).start();

        // 三、使用局部 synchronized 块
        C obj3 = new C();

        new Thread(obj3::a).start();
        new Thread(obj3::b).start();

        // 执行 SafeCalc
        SafeCalc safeCalc = new SafeCalc();
        List<Thread> ts = new ArrayList<>(100);
        for (int j = 0; j < 100;j++){
            Thread t = new Thread(() -> {
                for(int i = 0;i < 10; i++){
                    safeCalc.addOne();
                }
            });
            ts.add(t);
        }
        for(Thread t :ts){
            t.start();
        }
        //等待所有线程执行完成
        for(Thread t:ts){
            try{
                t.join();
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        System.out.println(safeCalc.get());
    }

}

