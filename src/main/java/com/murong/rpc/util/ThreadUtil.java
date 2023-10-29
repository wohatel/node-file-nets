package com.murong.rpc.util;

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
     * @param supplier
     */
    public static void execSilentVoid(VoidSupplier supplier) {
        try {
            supplier.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取
     *
     * @param supplier
     */
    public static boolean tryExecSilent(VoidSupplier supplier) {
        try {
            supplier.get();
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
     * 获取
     *
     * @param supplier
     */
    public static void execSilentVoidFinally(VoidSupplier supplier, VoidSupplier finallySupplier) {

        try {
            supplier.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                finallySupplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 执行异常
     *
     * @param supplier
     * @param e
     */
    public static void execSilentException(VoidSupplier supplier, Consumer<Exception> e) {
        try {
            supplier.get();
        } catch (Exception ex) {
            execSilentVoid(() -> e.accept(ex));
        }
    }

    /**
     * '
     * 执行多次
     *
     * @param supplier
     * @param times
     */
    public static void execSilentVoid(VoidSupplier supplier, int times) {
        for (int i = 0; i < times; i++) {
            execSilentVoid(supplier);
        }
    }

}
