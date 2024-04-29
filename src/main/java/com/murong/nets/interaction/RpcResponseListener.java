package com.murong.nets.interaction;

/**
 * 返回结果监听
 */
public interface RpcResponseListener {
    /**
     * 响应结果的处理
     *
     * @param response
     */
    void handle(RpcResponse response);
}
