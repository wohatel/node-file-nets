package com.murong.rpc.interaction;

import lombok.Data;

@Data
public class RpcFileRequest extends RpcRequest {
    private boolean finished;//是否传输完毕
    private long position;  //当前传输的内容位置
    private long length;    //文件总大小
    private String hash;    //文件的摘要
    private String fileName;//文件名称
    private byte[] bytes;   //此次传输文件的大小
    private String targetFilePath;   //目标路径

}
