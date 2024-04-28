package com.murong.rpc.server.handler;

import com.murong.rpc.interaction.RpcRequest;
import com.murong.rpc.service.RpcMsgService;
import com.murong.rpc.util.SpringContextHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


@ChannelHandler.Sharable
public class RpcServerRequestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest request = (RpcRequest) msg;
        RpcMsgService rpcMsgService = SpringContextHolder.getBean(RpcMsgService.class);
        rpcMsgService.exec(ctx, request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }
}
