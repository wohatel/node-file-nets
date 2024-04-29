package com.murong.nets;

import com.murong.nets.client.RpcHeartClient;
import com.murong.nets.interaction.ThreadUtil;
import com.murong.nets.util.RpcException;

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
