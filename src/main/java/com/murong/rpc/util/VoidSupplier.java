package com.murong.rpc.util;

/**
 * 空供应者
 */
public interface VoidSupplier {
    /**
     * 获取
     */
    void get() throws Exception;
}
