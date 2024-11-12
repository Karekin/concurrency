package com.example.concurrency.features.completablefuture;

import com.example.concurrency.util.ThreadUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 描述:
 * CompletableFutureExample 展示 Java 8 中的异步编程，它可以描述串行关系、AND汇聚关系和OR汇聚关系以及异常处理

 * 此代码展示了 CompletableFuture 的三种关系及异常处理：
 * 串行关系：多个任务按顺序执行。
 * 汇聚 AND 关系：两个任务都完成后再执行下一任务。
 * 汇聚 OR 关系：任意一个任务完成即可触发下一任务。
 * 异常处理：通过 exceptionally 为异常情况提供默认返回值。
 *
 */
public class CompletableFutureExample {
    /**
     * 任务 1：洗水壶 -> 烧开水
     * 使用 runAsync 创建一个异步无返回值的任务
     */
    private CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
        System.out.println("T1: 洗水壶...");
        ThreadUtil.sleep(1, TimeUnit.SECONDS); // 模拟洗水壶的耗时操作
        System.out.println("T1: 烧开水...");
        ThreadUtil.sleep(15, TimeUnit.SECONDS); // 模拟烧水的耗时操作
    });
    /**
     * 任务 2：洗茶壶 -> 洗茶杯 -> 拿茶叶
     * 使用 supplyAsync 创建一个异步带返回值的任务
     */
    private CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
        System.out.println("T2: 洗茶壶...");
        ThreadUtil.sleep(1, TimeUnit.SECONDS); // 模拟洗茶壶的耗时操作

        System.out.println("T2: 洗茶杯...");
        ThreadUtil.sleep(2, TimeUnit.SECONDS); // 模拟洗茶杯的耗时操作

        System.out.println("T2: 拿茶叶...");
        ThreadUtil.sleep(1, TimeUnit.SECONDS); // 模拟拿茶叶的耗时操作
        return "龙井"; // 返回茶叶名称
    });
    /**
     * 任务 3：在任务 1 和任务 2 完成后执行：泡茶
     * 使用 thenCombine 将任务 f1 和 f2 的结果结合起来
     */
    private CompletableFuture<String> f3 = f1.thenCombine(f2, (e, tf) -> {
        /*
            e 代表任务 f1 的执行结果。因为 f1 是一个 CompletableFuture<Void>，
                即不返回具体的值，所以 e 实际上是 null，仅表示 f1 已完成。
            tf 代表任务 f2 的执行结果。由于 f2 的返回类型为 CompletableFuture<String>，
                tf 的值为 f2 完成后的返回结果，即 "龙井"（表示茶叶的名称）。
         */
        System.out.println("T1: 拿到茶叶:" + tf);
        System.out.println("T1: 泡茶...");
        return "上茶:" + tf; // 返回泡好的茶
    });

    public static void main(String[] args) {
        CompletableFutureExample example = new CompletableFutureExample();
        System.out.println(example.f3.join()); // 阻塞主线程，直到 f3 完成并返回结果
    }

    /**
     * 串行关系示例: thenApply 串行执行多个任务
     */
    static class SerialRelation{
        private static CompletableFuture<String> f0 =
                CompletableFuture.supplyAsync(
                                () -> "Hello World") //① 异步任务1：返回字符串 "Hello World"
                        .thenApply(s -> s + " QQ")  //② 将结果拼接上 " QQ"
                        .thenApply(String::toUpperCase); //③ 将结果转换为大写

        public static void main(String[] args) {
            System.out.println(SerialRelation.f0.join()); // 输出 "HELLO WORLD QQ"
        }
    }

    /**
     * 汇聚 OR 关系示例: applyToEither 表示两个任务中任意一个完成即可
     */
    static class ConvergeRelation{
        static CompletableFuture<String> f1 =
                CompletableFuture.supplyAsync(() -> {
                    int t = getRandom(5, 10); // 生成 5 到 10 秒的随机等待时间
                    ThreadUtil.sleep(t, TimeUnit.SECONDS); // 模拟等待
                    return String.valueOf(t); // 返回等待的秒数
                });

        static CompletableFuture<String> f2 =
                CompletableFuture.supplyAsync(() -> {
                    int t = getRandom(5, 10); // 生成 5 到 10 秒的随机等待时间
                    ThreadUtil.sleep(t, TimeUnit.SECONDS); // 模拟等待
                    return String.valueOf(t); // 返回等待的秒数
                });

        static CompletableFuture<String> f3 =
                f1.applyToEither(f2, s -> s); // 任务 f3 取 f1 和 f2 中任意一个首先完成的结果

        private static int getRandom(int i, int j) {
            return (int) (Math.random() * (j - i)) + i; // 生成 i 到 j 之间的随机整数
        }

        public static void main(String[] args) {
            System.out.println(ConvergeRelation.f3.join()); // 输出最先完成的任务的结果（秒数）
        }
    }

    /**
     * 异常处理示例: exceptionally 处理异常，若发生异常则返回默认值
     */
    static class ExceptionHandler{
        private static CompletableFuture<Integer> f0 = CompletableFuture
                .supplyAsync(() -> 7 / 0) // 计算 7 / 0 会抛出异常
                .thenApply(r -> r * 10)   // 若无异常将结果乘以10
                .exceptionally(e -> 0);   // 若发生异常则返回 0 作为默认值

        public static void main(String[] args) {
            System.out.println(ExceptionHandler.f0.join()); // 输出 0
        }
    }
}
