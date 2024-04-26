package com.msb.mall.search;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class ThreadTest implements Callable<Integer> {

    // 定义一个线程池对象
    private static ExecutorService service = Executors.newFixedThreadPool(5);

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5
            , 50
            , 10
            , TimeUnit.SECONDS
            , new LinkedBlockingQueue<>(100)
            , Executors.defaultThreadFactory()
            , new ThreadPoolExecutor.AbortPolicy());


    @Test
    void testCompletable() throws Exception {
        CompletableFuture completableFuture = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程:" + Thread.currentThread().getName() + "线程开始");
            int i = 100 / 50;
            System.out.println("当前线程:" + Thread.currentThread().getName() + "线程结束");
        }, executor);

        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程:" + Thread.currentThread().getName() + "线程开始");
            int i = 100 / 50;
            System.out.println("当前线程:" + Thread.currentThread().getName() + "线程结束");
            return i;
        }, executor);
        System.out.println("获取的线程的返回结果是：" + future.get() );
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("当前线程:" + Thread.currentThread().getName());
        return 10;
    }

    @Test
    void testThread() throws Exception {
        Thread01 thread01 = new Thread01();
        thread01.start();

        Thread02 thread02 = new Thread02();
        new Thread(thread02).start();

        new Thread(() -> {
            System.out.println("当前线程 : " + Thread.currentThread().getName());
        });
//        FutureTask 本质上是一个Runnable接口
        FutureTask futureTask = new FutureTask(this);
        new Thread(futureTask).start();


        // 阻塞等待子线程的执行完成，然后获取线程的返回结果
        Object o = futureTask.get();
        System.out.println("o = " + o);
        System.out.println("main方法结束了...");

        service.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("线程池--》当前线程:" + Thread.currentThread().getName());
            }
        });

//        new ThreadPoolExecutor()


    }
}

class Thread01 extends Thread {
    @Override
    public void run() {
        System.out.println("当前线程:" + Thread.currentThread().getName());
    }
}

class Thread02 implements Runnable {

    @Override
    public void run() {
        System.out.println("当前线程:" + Thread.currentThread().getName());
    }
}

