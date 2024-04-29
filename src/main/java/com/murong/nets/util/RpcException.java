package com.murong.nets.util;

/**
 * description
 *
 * @author yaochuang 2024/04/29 11:47
 */
public class RpcException extends RuntimeException {

    public RpcException(Exception e) {
        super(e);
    }

    public RpcException(String message) {
        super(message);
    }
}
