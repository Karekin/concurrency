package com.example.concurrency.fucking;

import java.util.Random;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)

 *
    这段代码展示了一个典型的竞态条件问题，即组合状态不一致的问题。
    CombinationStatTask 线程检查两个变量 state1 和 state2 的组合状态， 但由于数据竞争，组合状态可能出现不一致的情况。
    这段代码模拟了多个线程操作共享变量的情形，即使变量被声明为 volatile 也无法解决问题，
    因为 volatile 只能保证变量自身的可见性，而不能保证两个变量之间的原子性更新。

    一、问题分析
    两个变量的组合状态要求：

    state1 和 state2 是组合状态，要求 state2 始终等于 state1 * 2。
    但是在多线程环境中，main 线程和 CombinationStatTask 线程同时操作这两个变量，
    可能导致 CombinationStatTask 在读取 state1 和 state2 时，
    正好遇到 main 线程更新了其中一个变量而尚未更新另一个变量的情形。

    二、为什么 volatile 无法解决问题：
    volatile 仅保证单个变量的可见性和禁止重排序，但无法保证多个变量的原子性操作。
    在 CombinationStatTask 的 run 方法中，读取 state1 和 state2 是两个独立的步骤。
    即使 state1 和 state2 是 volatile 的，也可能在读取 state1 和读取 state2 之间被 main 线程修改，
    导致读取到的 state1 和 state2 不匹配。

    三、代码中的竞态条件：
    由于 main 线程在循环中不断更新 state1 和 state2，而 CombinationStatTask 线程在检查它们的组合状态时，
    可能正好在两个更新操作之间执行检查，导致组合状态不一致。

    三、解决方案
    1、使用锁或同步块：
    通过 synchronized 或 ReentrantLock 确保 state1 和 state2 的读取和写入是一个原子操作，
    即每次检查和更新时，要么同时读取或更新 state1 和 state2，要么不执行。这种方式可以防止在读取和写入之间产生不一致的组合状态。

    2、将两个状态封装在一个对象中：
    可以用一个不可变的对象（如包含 state1 和 state2 的数据结构）表示组合状态，
    每次修改时创建一个新的对象，从而确保读取到的组合状态是一致的。
 */
@SuppressWarnings("InfiniteLoopStatement")
public class InvalidCombinationStateDemo {
    public static void main(String[] args) {
        CombinationStatTask task = new CombinationStatTask();
        Thread thread = new Thread(task);
        thread.start();

        Random random = new Random();
        while (true) {
            int rand = random.nextInt(1000);
            task.state1 = rand;
            task.state2 = rand * 2;
//            task.updateState(rand);
        }
    }

    private static class CombinationStatTask implements Runnable {
        // For combined state, adding volatile does not solve the problem
        volatile int state1;
        volatile int state2;

//        synchronized void updateState(int value) {
//            state1 = value;
//            state2 = value * 2;
//        }
//
//        synchronized int[] getState() {
//            return new int[]{state1, state2};
//        }

        @Override
        public void run() {
            int c = 0;
            for (long i = 0; ; i++) {
                int i1 = state1;
                int i2 = state2;
//                int[] states = getState();
//                int i1 = states[0];
//                int i2 = states[1];
                if (i1 * 2 != i2) {
                    c++;
                    System.err.printf("Fuck! Got invalid CombinationStat!! check time=%s, happen time=%s(%s%%), count value=%s|%s%n",
                            i + 1, c, (float) c / (i + 1) * 100, i1, i2);
                } else {
                    // if remove blew output,
                    // the probability of invalid combination on my dev machine goes from ~5% to ~0.1%
                    System.out.printf("Emm... %s|%s%n", i1, i2);
                }
            }
        }
    }

}
