package com.murong.rpc.util;

import java.io.IOException;

/**
 * 空供应者
 */
public interface VoidSupplier {
    /**
     * 获取
     */
    void get() throws Exception;
}
