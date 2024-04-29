package com.murong.nets.client.handler;

import com.murong.nets.interaction.RpcCommandType;
import com.murong.nets.interaction.RpcInteractionContainer;
import com.murong.nets.interaction.RpcMsg;
import com.murong.nets.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class RpcMessageClientInteractionHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(RpcMessageClientInteractionHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcMsg rpcMsg = JsonUtil.parseObject(msg, RpcMsg.class);
        if (rpcMsg.getRpcCommandType() == RpcCommandType.response) {
            RpcInteractionContainer.addResponse(rpcMsg.getResponse());
        } else if (rpcMsg.getRpcCommandType() == RpcCommandType.request) {
            ctx.fireChannelRead(rpcMsg.getRequest());
        } else if (rpcMsg.getRpcCommandType() == RpcCommandType.file) {
            FileClientChannelHandler.readFileRequest(ctx, rpcMsg.getRpcFileRequest());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("rpc信息异常:", cause);
    }

}
