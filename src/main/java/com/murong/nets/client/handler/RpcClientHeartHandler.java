/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package com.murong.nets.client.handler;


import com.murong.nets.client.RpcHeartClient;
import com.murong.nets.interaction.RpcMsgTransUtil;
import com.murong.nets.interaction.RpcRequest;
import com.murong.nets.interaction.RpcRequestType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * TCP message handler.
 *
 * @author murong 2018-08-03
 * @version 1.0
 */
public class RpcClientHeartHandler extends ChannelInboundHandlerAdapter {

    private final RpcHeartClient rpcHeartClient;

    public RpcClientHeartHandler(RpcHeartClient rpcHeartClient) {
        this.rpcHeartClient = rpcHeartClient;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent idle = (IdleStateEvent) evt;
        if (idle.state() == IdleState.WRITER_IDLE) {
            RpcRequest request = new RpcRequest();
            request.setRequestType(RpcRequestType.heart.name());
            RpcMsgTransUtil.sendMsg(ctx.channel(), request);
            rpcHeartClient.setWriteTime(System.currentTimeMillis());
        } else if (idle.state() == IdleState.READER_IDLE) {
            // 此时说明心跳已经超时
            rpcHeartClient.setReadOut(true);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

}
