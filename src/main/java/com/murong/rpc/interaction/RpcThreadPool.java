package com.murong.rpc.interaction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * rpc的线程池
 */
public class RpcThreadPool {
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static void setExecutorService(ExecutorService executorService) {
        RpcThreadPool.executorService = executorService;
    }
}
