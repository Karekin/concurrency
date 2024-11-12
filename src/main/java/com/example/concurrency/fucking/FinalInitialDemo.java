package com.example.concurrency.fucking;


/**
 * @author hyy (hjlbupt at 163 dot com)

 * 这段代码展示了指令重排序带来的并发问题。指令重排序可能会导致变量在未被正确初始化的情况下被访问，进而产生不可预期的行为。

 * 一、 问题分析（未复现并发问题）
 * 代码中的主要问题在于 reader 方法可能会在 writer 方法还未完全初始化对象时读取到不完整的 FinalInitialDemo 对象。
 *  由于指令重排序，a 和 flag 可能在 demo 赋值前被重排序，因此 reader 线程可能看到未初始化完全的对象，导致程序行为异常。

 * 二、 具体细节
 * 1. 指令重排序：
 *    - 在 writer 方法中，demo = new FinalInitialDemo() 可能会被编译器或 CPU 优化成以下顺序：
 *      1. 分配 FinalInitialDemo 对象的内存空间。
 *      2. 将 demo 引用指向该内存地址。
 *      3. 初始化成员变量 a 和 flag。
 *    - 在这种情况下，demo 引用已经指向了一个未初始化完全的对象，
 *          而此时 reader 线程可能会读取到一个 flag == true 且 a == 0 的对象。
 * 2. 并发影响：
 *    - 由于指令重排序，reader 线程可能在 flag == true 的条件下进入 if 语句并读取 a，
 *          但由于 a 可能尚未被赋值为 1，导致 a * a == 0 的情况。
 *    - 这会触发 System.out.println("Fuck! instruction reordering occurred.");，表示重排序导致了错误的状态。

 * 三、 解决方法
 * 为了避免这种指令重排序问题，可以将 a 和 flag 声明为 final，因为 final 变量在构造器内完成初始化后，
 *  不会被重新赋值，因此能够阻止重排序，确保其他线程总是看到正确的初始化状态。

 * 四、 总结
 * 这个代码展示了指令重排序带来的并发问题。将变量声明为 final 可以避免该问题，
 *  确保其他线程看到的 FinalInitialDemo 对象总是完整且正确初始化的。
 */
public class FinalInitialDemo {

    private int a;
    private boolean flag;
    private FinalInitialDemo demo;

    public FinalInitialDemo() {
        a = 1;
        flag = true;
    }

    public void writer() {
        demo = new FinalInitialDemo();
    }

    public void reader() {
        if (flag) {
            int i = a * a;
            if (i == 0) {
                // On my dev machine, the variable initial always success.
                // To solve this problem, add final to the `a` field and `flag` field.
                System.out.println("Fuck! instruction reordering occurred.");
            }
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws Exception {
        while (true) {
            FinalInitialDemo demo = new FinalInitialDemo();
            Thread threadA = new Thread(demo::writer);
            Thread threadB = new Thread(demo::reader);

            threadA.start();
            threadB.start();

            threadA.join();
            threadB.join();
        }
    }
}
