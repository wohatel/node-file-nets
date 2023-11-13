package com.murong.rpc.interaction;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Data
public class RpcFuture implements Future<RpcResponse> {
    private long timeOut = 8000l;
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

    public RpcResponse getResponse() {
        return response;
    }

    public void setResponse(RpcResponse response) {
        this.response = response;
    }

    @Override
    public RpcResponse get() throws InterruptedException {
        while (true) {
            if (isDone) {
                return response;
            }
            if (isCanceled) {
                if (mayInterruptIfRunning) {
                    throw new InterruptedException("is canceled");
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
        long l = unit.toMillis(timeout);
        this.timeOut = l;
        return get();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }


    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public long getReponseTime() {
        return reponseTime;
    }

    public void setReponseTime(long reponseTime) {
        this.reponseTime = reponseTime;
    }

    /**
     * 添加监听
     *
     * @param listener
     * @return
     */
    public RpcFuture addListener(RpcResponseListener listener) {
        this.listeners.add(listener);
        return this;
    }

}
