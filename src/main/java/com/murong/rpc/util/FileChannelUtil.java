package com.murong.rpc.util;

;
import com.murong.rpc.interaction.RpcFileRequest;
import com.murong.rpc.interaction.RpcMsgTransUtil;
import com.murong.rpc.interaction.RpcResponse;
import io.netty.channel.Channel;

import java.io.File;
import java.io.IOException;

public class FileChannelUtil {


    public static void readFileRequest(Channel channel, RpcFileRequest rpcFileRequest) throws IOException {
        if (rpcFileRequest.getPosition() == 0) {// 说明是首次上传,则应该是先删除
            File file = new File(rpcFileRequest.getTargetFilePath());
            if (file.exists()) {
                file.delete();
            }
        }
        String format = String.format("接受文件:%s 大小:%s 当前位置:%s", rpcFileRequest.getTargetFilePath(), rpcFileRequest.getLength(), rpcFileRequest.getPosition());
        RpcResponse rpcResponse = FileUtil.dealRpcFileRequest(rpcFileRequest);
        RpcMsgTransUtil.write(channel, rpcResponse);
    }
}
