package com.example.concurrency.fucking;

/**
 * @author Jerry Lee(oldratlee at gmail dot com)

 * 这段代码展示了在多线程环境下可能出现的并发问题，即数据竞争导致的“读取撕裂”（read tearing）问题。
 * 问题的根源在于`count`变量是`long`类型，而`long`变量在一些32位系统上可能不是原子操作，
 * 这意味着对`count`的写入和读取可能发生在不同的线程之间不一致。

 * 具体来说：
 * 1. `main`方法的循环中，不断更新`count`变量的高32位和低32位（通过左移和按位或操作）。
 * 2. 同时，另一个线程中`ConcurrencyCheckTask`任务持续读取`count`的值，并将其拆分成高位和低位进行比较。

 * 在没有合适同步的情况下，可能会出现以下情况：
 * - 写入`count`的操作被拆分为两步：首先写入高32位，然后写入低32位。
 * - 如果读取线程在写入线程执行高32位和低32位的写入之间读取了`count`，它可能会得到一个无效的`count`值，即高低位不一致的数值。
 * - 这种高低位不一致的读取会触发`if (high != low)`条件，从而输出错误信息。

 * 解决方法：使用`volatile`关键字将`count`声明为`volatile long`，
 *          或使用合适的同步方法来确保写入和读取是原子性的，以避免撕裂读取的情况。
 */
public class InvalidLongDemo {
//    volatile long count = 0;
    long count = 0;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        // LoadMaker.makeLoad();

        InvalidLongDemo demo = new InvalidLongDemo();

        Thread thread = new Thread(demo.getConcurrencyCheckTask());
        thread.start();

        for (int i = 0; ; i++) {
            @SuppressWarnings("UnnecessaryLocalVariable")
            final long l = i;
            /*
                生成一个包含相同高32位和低32位的 long 值，并赋值给 demo.count：
                l << 32：将 l 左移32位，把原来的低32位移到高32位的位置。
                l << 32 | l：将左移后的 l 与原值 l 进行按位或操作，将 l 的低32位保留在低32位上，
                    而高32位则由 l << 32 填充。这样 count 的高32位和低32位都等于 l。

                假设 i 是 42（即 0x0000002A），转换为 long 后 l = 0x000000000000002A。
                l << 32 结果是 0x0000002A00000000。
                l << 32 | l 的结果是 0x0000002A0000002A。
                这样，demo.count 的高32位和低32位都包含了同样的值 0x2A。

                在 Java 中，long 是 64 位的，每个 long 值用 16 个十六进制字符来表示（每个十六进制字符占 4 位），
                因此 0x0000002A00000000 实际上表示的确是一个 64 位的数。对于这个 64 位的数：
                0x0000002A00000000 的前 8 个字符 0000002A 是高 32 位。
                后 8 个字符 00000000 是低 32 位。
             */
            demo.count = l << 32 | l;
        }
    }

    ConcurrencyCheckTask getConcurrencyCheckTask() {
        return new ConcurrencyCheckTask();
    }

    private class ConcurrencyCheckTask implements Runnable {
        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            int c = 0;
            for (int i = 0; ; i++) {
                long l = count;
                /*
                    通过位操作将一个 64 位 long 类型的值 count 分解成高 32 位和低 32 位，
                        high 是 count 的高 32 位部分，low 是 count 的低 32 位部分：

                    1. long l = count;
                       将 count 的值赋给 l。count 是一个 64 位的 long 类型变量，包含了 64 位数据。

                    2. long high = l >>> 32;
                       这里使用了无符号右移操作符 >>>。
                       - l >>> 32 表示将 l 的值无符号右移 32 位，忽略符号位，左侧用 0 填充。
                       - 经过无符号右移后，l 的原始高 32 位移动到了低 32 位的位置，而高 32 位则被填充为 0。
                       - high 最终得到的是 count 的原始高 32 位的值，表示为一个 long 类型。

                    3. long low = l & 0xFFFFFFFFL;
                       使用按位与操作符 & 取得 l 的低 32 位：
                       - 0xFFFFFFFFL 是一个掩码（mask），它在二进制中表示低 32 位为 1，
                            高 32 位为 0，即 00000000FFFFFFFF。
                       - l & 0xFFFFFFFFL 的结果是 l 的低 32 位值，其余位都被掩盖为 0。
                       - 因此，low 得到了 count 的低 32 位。
                 */
                long high = l >>> 32;
                long low = l & 0xFFFFFFFFL;
                if (high != low) {
                    c++;
                    System.err.printf("Fuck! Got invalid long!! check time=%s, happen time=%s(%s%%), count value=%s|%s%n",
                            i + 1, c, (float) c / (i + 1) * 100, high, low);
                } else {
                    // If remove this output, invalid long is not observed on my dev machine
                    System.out.printf("Emm... %s|%s%n", high, low);
                }
            }
        }
    }

}
