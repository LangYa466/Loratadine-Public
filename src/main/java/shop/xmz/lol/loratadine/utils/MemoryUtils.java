package shop.xmz.lol.loratadine.utils;

import java.util.ArrayList;
import java.util.List;

public class MemoryUtils {
    //GPT编写
    public static void optimizeMemory(long cleanUpDelay) {
        try {
            // 请求垃圾回收器运行
            System.gc();

            // 强制进行 finalize() 方法调用
            System.runFinalization();

            // 创建多个线程进行优化
            List<Thread> threads = new ArrayList<>();
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            for (int i = 0; i < availableProcessors; i++) {
                Thread thread = new Thread(() -> {
                    // 休眠一段时间，让垃圾回收器有足够的时间进行清理
                    try {
                        Thread.sleep(cleanUpDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                threads.add(thread);
                thread.start();
            }

            // 等待所有优化线程完成
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void memoryCleanup(){
        //仅执行一次 请放在你要优化的地方调用[或者启动游戏函数]
        //Java 请求垃圾回收[优化内存]
        System.gc();
    }
}
