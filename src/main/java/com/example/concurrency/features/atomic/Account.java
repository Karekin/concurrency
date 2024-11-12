package com.example.concurrency.features.atomic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 该类复现了 ABA 问题，参考文章：<a href="https://www.cnblogs.com/BlogNetSpace/p/17143939.html">并发中的ABA问题</a>
 */

public class Account {
    private AtomicInteger balance;
    private AtomicInteger transactionCount;
    private ThreadLocal<Integer> currentThreadCASFailureCount;

    public Account(int initialBalance) {
        this.balance = new AtomicInteger(initialBalance);
        this.transactionCount = new AtomicInteger(0);
        this.currentThreadCASFailureCount = ThreadLocal.withInitial(() -> 0);
    }

    public int getBalance() {
        return balance.get();
    }

    public int getTransactionCount() {
        return transactionCount.get();
    }

    public int getCurrentThreadCASFailureCount() {
        return currentThreadCASFailureCount.get();
    }

    public boolean deposit(int amount) {
        int current = balance.get();
        boolean result = balance.compareAndSet(current, current + amount);
        if (result) {
            transactionCount.incrementAndGet();
        } else {
            int currentCASFailureCount = currentThreadCASFailureCount.get();
            currentThreadCASFailureCount.set(currentCASFailureCount + 1);
        }
        return result;
    }

    public boolean withdraw(int amount) {
        int current = getBalance();
        maybeWait();
        boolean result = balance.compareAndSet(current, current - amount);
        if (result) {
            transactionCount.incrementAndGet();
        } else {
            int currentCASFailureCount = currentThreadCASFailureCount.get();
            currentThreadCASFailureCount.set(currentCASFailureCount + 1);
        }
        return result;
    }

    private void maybeWait() {
        if ("thread1".equals(Thread.currentThread().getName())) {
            try {
                Thread.sleep(2000); // 休眠2秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

