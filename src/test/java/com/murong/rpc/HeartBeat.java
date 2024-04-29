package com.murong.rpc;

import com.murong.rpc.client.RpcHeartClient;
import com.murong.rpc.interaction.ThreadUtil;
import com.murong.rpc.util.RpcException;

public class HeartBeat {

    public static void main(String[] args) {
        RpcHeartClient client = new RpcHeartClient("127.0.0.1",8888);
        ThreadUtil.run(10000,()->{
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RpcException(e);
            }
        });
    }
}
