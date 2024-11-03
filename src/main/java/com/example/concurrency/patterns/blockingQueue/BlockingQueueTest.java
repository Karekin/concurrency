package com.example.concurrency.patterns.blockingQueue;

public class BlockingQueueTest {
    public static void main(String[] args) {
        BlockingQueue<Integer> q = new BlockingQueue<>(2);

        Thread t1 = new Thread(() -> {
            int[] data = {1, 2, 3, 4};
            for (int num : data) {
                try {
                    System.out.println(Thread.currentThread().getId() + ": push " + num);
                    q.push(num);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getId() + ": wait for 1s, then start pop");
                Thread.sleep(1000);
                for (int i = 0; i < 8; i++) {
                    Integer num = q.pop();
                    System.out.println(Thread.currentThread().getId() + ": pop = " + num);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread t3 = new Thread(() -> {
            try {
                Thread.sleep(2000);
                int[] data = {5, 6, 7, 8};
                for (int num : data) {
                    System.out.println(Thread.currentThread().getId() + ": push " + num);
                    q.push(num);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
