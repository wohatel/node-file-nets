package com.murong.nets.interaction;


import com.murong.nets.util.RpcException;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Data
public class RpcFuture implements Future<RpcResponse> {
    private long timeOut = 8000L;
    private long requestTime = System.currentTimeMillis();
    private long reponseTime;
    private String requestId;
    private RpcResponse response;
    private boolean isDone;
    private boolean isCanceled;
    private boolean mayInterruptIfRunning;

    /**
     * 事件监听
     */
    private List<RpcResponseListener> listeners = new ArrayList<>();

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.isCanceled = true;
        this.mayInterruptIfRunning = mayInterruptIfRunning;
        RpcInteractionContainer.remove(requestId);
        return isCanceled;
    }

    @Override
    public boolean isCancelled() {
        return this.isCanceled;
    }

    @Override
    public boolean isDone() {
        return this.isDone;
    }

    @SneakyThrows
    @Override
    public RpcResponse get() {
        while (true) {
            if (isDone) {
                return response;
            }
            if (isCanceled) {
                if (mayInterruptIfRunning) {
                    throw new RpcException("is canceled");
                }
                break;
            }
            long time = System.currentTimeMillis() - requestTime; // 已超时时间
            if (time >= timeOut) {
                break;
            }
            Thread.sleep(10);
        }
        return response;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException {
        this.timeOut = unit.toMillis(timeout);
        return get();
    }
}
