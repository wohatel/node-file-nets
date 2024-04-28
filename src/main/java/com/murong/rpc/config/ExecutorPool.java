package com.murong.rpc.config;

import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorPool {

    @Getter
    private static ExecutorService executorService = new ThreadPoolExecutor(1, 100, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(5000));

}
