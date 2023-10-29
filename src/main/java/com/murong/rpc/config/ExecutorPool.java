package com.murong.rpc.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorPool {


    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static void setExecutorService(ExecutorService executorService) {
        ExecutorPool.executorService = executorService;
    }
}
