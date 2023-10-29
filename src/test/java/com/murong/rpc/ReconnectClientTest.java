package com.murong.rpc;


import com.murong.rpc.client.RpcAutoReconnectClient;
import com.murong.rpc.interaction.*;

import java.util.concurrent.atomic.AtomicLong;

public class ReconnectClientTest {

    public static void test() {
        RpcAutoReconnectClient client = new RpcAutoReconnectClient("127.0.0.1", 8888);
        client.reConnect();

        AtomicLong atomicLong = new AtomicLong();
        ThreadUtil.run(30, () -> {
            long l1 = System.currentTimeMillis();
            while (true) {
                try {
                    StringBuilder sb = new StringBuilder("我");
                    RpcRequest rpcFileRequest = new RpcRequest();
                    rpcFileRequest.setBody(sb.toString());
                    long l = atomicLong.addAndGet(1l);
                    if (l % 1000 == 0) {
//                        RpcFuture rpcFuture = client.sendSynMsg(rpcFileRequest);
//                        RpcResponse rpcResponse = rpcFuture.get();
//                        System.out.println(rpcResponse.getBody());
                        long l2 = System.currentTimeMillis();
                        System.out.println(l * 1000 / (l2 - l1));
                        client.closeChannel();
                    } else {
                        RpcFuture rpcFuture = client.sendSynMsg(rpcFileRequest);
                        rpcFuture.get();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        test();
    }

}
