package com.murong.nets.interaction;

import com.murong.nets.config.ExecutorPool;
import com.murong.nets.util.JsonUtil;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yaochuang
 */
public class RpcInteractionContainer {
    @Getter
    private static final ConcurrentHashMap<String, RpcFuture> futureMap = new ConcurrentHashMap<>();

    public static RpcFuture addRequest(RpcRequest rpcRequest) {
        if (rpcRequest == null) {
            return null;
        }
        if (rpcRequest.getRequestId() == null) {
            return null;
        }
        RpcFuture rpcFuture = new RpcFuture();
        rpcFuture.setRequestId(rpcRequest.getRequestId());
        futureMap.put(rpcRequest.getRequestId(), rpcFuture);
        return rpcFuture;
    }

    public static void addResponse(RpcResponse rpcResponse) {
        if (rpcResponse == null) {
            return;
        }
        if (rpcResponse.getRequestId() == null) {
            return;
        }
        RpcFuture rpcFuture = futureMap.remove(rpcResponse.getRequestId());
        if (rpcFuture == null) { // 可能超时已被移除
            return;
        }
        rpcFuture.setReponseTime(System.currentTimeMillis());
        rpcFuture.setResponse(rpcResponse);
        rpcFuture.setDone(true);
        List<RpcResponseListener> listeners = rpcFuture.getListeners();
        if (!CollectionUtils.isEmpty(listeners)) {
            for (RpcResponseListener rpcResponseListener : listeners) {
                ExecutorPool.getExecutorService().submit(() -> rpcResponseListener.handle(rpcResponse)); // 处理响应事件
            }
        }
    }

    public static void addResponse(String rpcResponseString) {
        if (rpcResponseString == null) {
            return;
        }
        addResponse(JsonUtil.parseObject(rpcResponseString, RpcResponse.class));
    }

    public static RpcFuture addRequest(RpcRequest rpcRequest, long timeOut) {
        if (rpcRequest == null) {
            return null;
        }
        if (rpcRequest.getRequestId() == null) {
            return null;
        }
        RpcFuture rpcFuture = new RpcFuture();
        rpcFuture.setRequestId(rpcRequest.getRequestId());
        if (timeOut > 0) {
            rpcFuture.setTimeOut(timeOut);
        }
        futureMap.put(rpcRequest.getRequestId(), rpcFuture);
        return rpcFuture;
    }

    public static RpcFuture remove(String requestId) {
        return futureMap.remove(requestId);
    }


    public static int concurrentSize() {
        return futureMap.size();
    }

}
