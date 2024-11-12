package com.example.concurrency;

import com.example.concurrency.features.atomic.Account;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * 这个测试中的断言 assertEquals(1000 - amountToWithdrawByThread1, account.getBalance());
 * 的确会因为 ABA 问题而偶尔失败。当 thread1 在 thread2 之前执行时，ABA 问题就会导致 balance 出现意外的状态，使得断言失败。

 * 具体来说，这里出现的问题正好符合ABA问题的特征：thread1 执行 withdraw 操作后，balance 减少了 100（从1000到900）。
 * 而在 thread1 的 maybeWait 延迟期间，thread2 执行了 deposit 和 withdraw 操作，
 * 这使得 balance 先增加 50（到1050），再减去 50（回到1000），最终看起来似乎没有变化。
 * 这种情况导致了thread1 原本的检查不再可靠，balance 在 thread1 再次尝试 compareAndSet 时已经回到了初始值。

 * 因此，thread1 检查 balance 时虽然它仍然是1000，但它忽略了中间的变化，这就是 ABA 问题的典型表现。
 * 当 thread2 在 thread1 之前完成时，不会发生这个问题，assertEquals 断言也不会失败。
 */
public class ABATest {
    // ABA 问题测试
    @Test
    public void abaProblemTest() throws InterruptedException {
        Account account = new Account(1000);

        int amountToWithdrawByThread1 = 100;
        int amountToDepositByThread2 = 50;
        int amountToWithdrawByThread2 = 50;

        Runnable thread1 = () -> {
            assertTrue(account.withdraw(amountToWithdrawByThread1));
            assertTrue(account.getCurrentThreadCASFailureCount() > 0); // 测试将失败！
        };

        Runnable thread2 = () -> {
            assertTrue(account.deposit(amountToDepositByThread2));
            assertEquals(1000 + amountToDepositByThread2, account.getBalance());
            assertTrue(account.withdraw(amountToWithdrawByThread2));
            assertEquals(1000, account.getBalance());
            assertEquals(0, account.getCurrentThreadCASFailureCount());
        };

        Thread t1 = new Thread(thread1, "thread1");
        Thread t2 = new Thread(thread2, "thread2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        assertEquals(1000 - amountToWithdrawByThread1, account.getBalance());
        assertEquals(3, account.getTransactionCount());
    }
}
