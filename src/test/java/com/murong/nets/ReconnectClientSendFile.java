package com.murong.nets;


import com.murong.nets.client.RpcAutoReconnectClient;

import java.io.IOException;

public class ReconnectClientSendFile {

    public static void test() throws InterruptedException, IOException {
        RpcAutoReconnectClient client = new RpcAutoReconnectClient("127.0.0.1", 8888);
        client.reConnect();

        Thread.sleep(2000);
        System.out.println(System.currentTimeMillis());
        client.sendDir("/Users/yaochuang/Desktop/", "/Users/yaochuang/rpc_test", 1024);
        System.out.println("完毕");
        System.out.println(System.currentTimeMillis());

    }


    public static void main(String[] args) throws IOException, InterruptedException {
        test();
    }
}
