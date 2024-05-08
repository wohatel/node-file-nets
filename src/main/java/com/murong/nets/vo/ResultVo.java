package com.murong.nets.vo;

import com.murong.nets.config.CodeConfig;
import lombok.Data;

import java.util.function.Supplier;

/**
 * 响应结果的vo
 *
 * @param <T>
 */
@Data
public class ResultVo<T> {

    private int code = CodeConfig.SUCCESS;
    private T data;

    private boolean success = true;

    private String msg;


    /**
     * 返回标准结果
     *
     * @param supplier 泛型参数
     * @param <T>      泛型
     * @return 实例
     */
    public static <T> ResultVo<T> supplier(Supplier<T> supplier) {
        ResultVo<T> resultVo = new ResultVo<>();
        resultVo.setData(supplier.get());
        return resultVo;
    }
}

