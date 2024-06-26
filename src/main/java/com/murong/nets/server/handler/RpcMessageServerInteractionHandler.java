package com.murong.nets.server.handler;

import com.murong.nets.interaction.RpcCommandType;
import com.murong.nets.interaction.RpcInteractionContainer;
import com.murong.nets.interaction.RpcMsg;
import com.murong.nets.util.FileChannelUtil;
import com.murong.nets.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.java.Log;

@ChannelHandler.Sharable
@Log
public class RpcMessageServerInteractionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcMsg rpcMsg = JsonUtil.parseObject(msg, RpcMsg.class);
        if (rpcMsg.getRpcCommandType() == RpcCommandType.response) {
            RpcInteractionContainer.addResponse(rpcMsg.getResponse());
        } else if (rpcMsg.getRpcCommandType() == RpcCommandType.request) {
            ctx.fireChannelRead(rpcMsg.getRequest());
        } else if (rpcMsg.getRpcCommandType() == RpcCommandType.file) {
            FileChannelUtil.readFileRequest(ctx.channel(), rpcMsg.getRpcFileRequest());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warning(cause.getMessage());
    }

}
