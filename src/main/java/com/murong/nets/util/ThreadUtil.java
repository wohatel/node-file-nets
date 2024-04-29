package com.murong.nets.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 解析工具
 */
public final class ThreadUtil {

    /**
     * 无参构造器
     */
    private ThreadUtil() {
    }

    /**
     * 获取
     */
    public static <T> T execSilent(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取
     *
     * @param runnable
     */
    public static void execSilentVoid(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取
     *
     * @param runnable
     */
    public static boolean tryExecSilent(Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取
     *
     * @param supplier
     */
    public static <T> void execSilentFinally(Supplier<T> supplier, Consumer<T> consumer) {
        T t = null;
        try {
            t = supplier.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.accept(t);
        }
    }

    /**
     * 获取
     *
     * @param supplier
     */
    public static <T> T execSilentExceptionToNull(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 执行异常
     *
     * @param runnable 运行方法
     */
    public static void execSilentException(Runnable runnable, Consumer<Exception> e) {
        try {
            runnable.run();
        } catch (Exception ex) {
            execSilentVoid(() -> e.accept(ex));
        }
    }

    /**
     * '
     * 执行多次
     *
     * @param runnable 方法
     * @param times    次数
     */
    public static void execSilentVoid(Runnable runnable, int times) {
        for (int i = 0; i < times; i++) {
            execSilentVoid(runnable);
        }
    }

}
