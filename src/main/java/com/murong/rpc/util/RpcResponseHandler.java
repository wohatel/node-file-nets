package com.murong.rpc.util;

import com.murong.rpc.interaction.RpcResponse;

import java.util.function.Function;

public class RpcResponseHandler {

    public static <T> T handler(RpcResponse response, Function<String, T> function) {
        if (!StringUtil.isBlank(response.getMsg())) {
            throw new RuntimeException(response.getMsg());
        }
        return function.apply(response.getBody());
    }

}
