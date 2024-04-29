package com.murong.nets.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.concurrent.TimeUnit;

/**
 * @author yaochuang
 */
@Getter
@Setter
@Log
public class RpcAutoReconnectClient extends RpcDefaultClient {

    /**
     * 是否允许自动重连
     */
    private boolean allowAutoConnect = true;

    public RpcAutoReconnectClient(String host, int port, NioEventLoopGroup nioEventLoopGroup) {
        super(host, port, nioEventLoopGroup);
    }

    public RpcAutoReconnectClient(String host, int port) {
        this(host, port, new NioEventLoopGroup());
    }

    public void reConnect() {
        if (!allowAutoConnect) {
            return; // 销毁后,禁止重连
        }
        ChannelFuture future = this.connect();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if (futureListener.isSuccess()) {
                    channel.closeFuture().addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            reConnect();
                        }
                    });
                } else {
                    futureListener.channel().eventLoop().schedule(() -> {
                        try {
                            reConnect();
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }, 10, TimeUnit.SECONDS);
                }
            }
        });
    }

    @Override
    public void closeChannel() {
        this.allowAutoConnect = false; // 不允许重连
        if (channel != null) {
            channel.close();
        }
    }
}
