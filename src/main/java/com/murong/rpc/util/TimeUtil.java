package com.murong.rpc.util;

import java.util.function.Supplier;

public class TimeUtil {

    /**
     * 每隔一段时间执行一次
     */
    public static void execByInterval(Runnable runnable, long timeMillis) {
        while (true) {
            runnable.run();
            try {
                Thread.sleep(timeMillis);
            } catch (Exception e) {

            }
        }
    }


    /**
     * 每隔一段时间执行一次,supplier结果为true
     */
    public static void execDapByFunction(Supplier<Boolean> supplier, long timeMillis) {
        while (true) {
            Boolean result = false;
            try {
                result = supplier.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result) {
                break;
            }
            try {
                Thread.sleep(timeMillis);
            } catch (Exception e) {

            }
        }
    }

    /**
     * 每隔一段时间执行一次,supplier结果为true
     */
    public static void execDapByFunction(Supplier<Boolean> supplier, long timeMillis, int maxTimes) {
        for (int i = 0; i < maxTimes; i++) {
            Boolean aBoolean = supplier.get();
            if (aBoolean) {
                return;
            }
            try {
                Thread.sleep(timeMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
