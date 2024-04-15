package com.murong.rpc.util;

import com.murong.rpc.interaction.RpcResponse;

import java.util.function.Function;

/**
 * response结果处理器
 *
 * @author yaochuang 2024/04/12 17:26
 */
public class RpcResponseHandler {

    public static <T> T handler(RpcResponse response, Function<String, T> function) {
        if (!StringUtil.isBlank(response.getMsg())) {
            throw new RuntimeException(response.getMsg());
        }
        return function.apply(response.getBody());
    }

}
