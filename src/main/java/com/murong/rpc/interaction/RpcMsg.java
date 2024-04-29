package com.murong.rpc.interaction;

import com.murong.rpc.util.RpcException;
import lombok.Data;

import java.util.Objects;

@Data
public class RpcMsg {
    private RpcCommandType rpcCommandType;
    private RpcRequest request;
    private RpcResponse response;
    private RpcFileRequest rpcFileRequest;

    public static RpcMsg build(Object object) {
        Objects.requireNonNull(object);
        if (object instanceof RpcFileRequest) {
            return new RpcMsg((RpcFileRequest) object);
        } else if (object instanceof RpcRequest) {
            return new RpcMsg((RpcRequest) object);
        } else if (object instanceof RpcResponse) {
            return new RpcMsg((RpcResponse) object);
        }
        throw new RpcException("类型不支持");
    }

    public RpcMsg(RpcRequest request) {
        rpcCommandType = RpcCommandType.request;
        this.request = request;
    }

    public RpcMsg(RpcResponse response) {
        rpcCommandType = RpcCommandType.response;
        this.response = response;
    }

    public RpcMsg(RpcFileRequest rpcFileRequest) {
        rpcCommandType = RpcCommandType.file;
        this.rpcFileRequest = rpcFileRequest;
    }

}
