package com.murong.nets.server.handler;

import com.murong.nets.interaction.RpcRequest;
import com.murong.nets.service.RpcMsgService;
import com.murong.nets.util.SpringContextHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.java.Log;


@ChannelHandler.Sharable
@Log
public class RpcServerRequestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest request = (RpcRequest) msg;
        RpcMsgService rpcMsgService = SpringContextHolder.getBean(RpcMsgService.class);
        rpcMsgService.exec(ctx, request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warning(cause.getMessage());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }
}
