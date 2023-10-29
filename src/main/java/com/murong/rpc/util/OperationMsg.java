package com.murong.rpc.util;

/**
 * 操作结果
 */
public class OperationMsg {

    private boolean operateStatus = true;

    private String msg;

    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isOperateStatus() {
        return operateStatus;
    }

    public void setOperateStatus(boolean operateStatus) {
        this.operateStatus = operateStatus;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
