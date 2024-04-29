package com.murong.nets.util;

import com.murong.nets.interaction.RpcFileRequest;
import com.murong.nets.interaction.RpcMsgTransUtil;
import com.murong.nets.interaction.RpcResponse;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * 文件通道处理工具
 *
 * @author yaochuang 2024/04/12 17:21
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileChannelUtil {


    public static void readFileRequest(Channel channel, RpcFileRequest rpcFileRequest) throws IOException {
        RpcResponse rpcResponse = FileUtil.dealRpcFileRequest(rpcFileRequest);
        RpcMsgTransUtil.write(channel, rpcResponse);
    }
}
