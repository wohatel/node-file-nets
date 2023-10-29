package com.murong.rpc.server.handler;

import com.murong.rpc.client.ClientSitePool;
import com.murong.rpc.client.RpcDefaultClient;
import com.murong.rpc.config.CodeConfig;
import com.murong.rpc.config.EnvConfig;
import com.murong.rpc.config.ExecutorPool;
import com.murong.rpc.constant.RequestTypeEnmu;
import com.murong.rpc.interaction.RpcMsgTransUtil;
import com.murong.rpc.interaction.RpcRequest;
import com.murong.rpc.interaction.RpcRequestType;
import com.murong.rpc.interaction.RpcResponse;
import com.murong.rpc.service.RpcMsgService;
import com.murong.rpc.util.JsonUtil;
import com.murong.rpc.util.OperationMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.util.List;


@ChannelHandler.Sharable
public class RpcServerRequestHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest request = (RpcRequest) msg;
        RpcMsgService.exec(ctx, request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }
}
