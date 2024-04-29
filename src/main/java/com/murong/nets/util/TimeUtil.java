package com.murong.nets.util;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Log
public class TimeUtil {

    /**
     * 每隔一段时间执行一次,supplier结果为true
     */
    @SneakyThrows
    public static void execDapByFunction(Supplier<Boolean> supplier, long timeMillis) {
        while (true) {
            Boolean result = false;
            try {
                result = supplier.get();
            } catch (Exception e) {
                log.warning(e.getMessage());
            }
            if (result) {
                break;
            }

            Thread.sleep(timeMillis);

        }
    }

    /**
     * 每隔一段时间执行一次,supplier结果为true
     */
    @SneakyThrows
    public static void execDapByFunction(BooleanSupplier supplier, long timeMillis, int maxTimes) {
        for (int i = 0; i < maxTimes; i++) {
            boolean aBoolean = supplier.getAsBoolean();
            if (aBoolean) {
                return;
            }
            Thread.sleep(timeMillis);
        }
    }
}
