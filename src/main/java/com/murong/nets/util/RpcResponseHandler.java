package com.murong.nets.util;

import com.murong.nets.interaction.RpcResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Function;

/**
 * response结果处理器
 *
 * @author yaochuang 2024/04/12 17:26
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpcResponseHandler {

    public static <T> T handler(RpcResponse response, Function<String, T> function) {
        if (!StringUtil.isBlank(response.getMsg())) {
            throw new RpcException(response.getMsg());
        }
        return function.apply(response.getBody());
    }

}
