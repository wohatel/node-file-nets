package com.murong.rpc.vo;

import com.murong.rpc.config.CodeConfig;

import java.util.function.Supplier;

/**
 * 响应结果的vo
 *
 * @param <T>
 */
public class ResultVo<T> {

    private int code = CodeConfig.SUCCESS;
    private T data;

    private boolean success = true;

    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 返回标准结果
     *
     * @param supplier
     * @param <T>
     * @return
     */
    public static <T> ResultVo<T> supplier(Supplier<T> supplier) {
        ResultVo<T> resultVo = new ResultVo<>();
        resultVo.setData(supplier.get());
        return resultVo;
    }
}

