package com.murong.nets.initializer;

import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.Arrays;
import java.util.List;


public class StringChannelInitializer extends ChannelInitializer<SocketChannel> {

    private List<ChannelHandler> channelHandlers;

    public StringChannelInitializer(ChannelHandler... channelHandlers) {
        if (channelHandlers != null && channelHandlers.length > 0) {
            this.channelHandlers = Arrays.asList(channelHandlers);
        }
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));

        if (channelHandlers != null) {
            for (ChannelHandler channelHandler : channelHandlers) {
                pipeline.addLast(channelHandler);
            }
        }
    }
}
