package com.murong.nets.util;

import com.murong.nets.interaction.RpcFileRequest;
import com.murong.nets.interaction.RpcMsgTransUtil;
import com.murong.nets.interaction.RpcResponse;
import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;

;

/**
 * 文件通道处理工具
 *
 * @author yaochuang 2024/04/12 17:21
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileChannelUtil {


    public static void readFileRequest(Channel channel, RpcFileRequest rpcFileRequest) throws IOException {
        // 说明是首次上传,则应该是先删除
        if (rpcFileRequest.getPosition() == 0) {
            File file = new File(rpcFileRequest.getTargetFilePath());
            if (file.exists()) {
                file.delete();
            }
        }
        RpcResponse rpcResponse = FileUtil.dealRpcFileRequest(rpcFileRequest);
        RpcMsgTransUtil.write(channel, rpcResponse);
    }
}
