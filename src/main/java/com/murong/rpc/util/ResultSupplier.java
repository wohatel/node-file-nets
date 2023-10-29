package com.murong.rpc.util;

/**
 * 空供应者
 */
public interface ResultSupplier<T> {
    /**
     * 获取
     */
    T get() throws Exception;
}
