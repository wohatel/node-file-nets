package com.murong.rpc.config;

import com.murong.rpc.vo.ResultVo;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 异常通知
 */
@ControllerAdvice
public class RestExceptionHandler {

    /**
     * catched
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object catched(Exception ex) {
        ResultVo<String> vo = new ResultVo<>();
        vo.setCode(CodeConfig.ERROR);
        vo.setSuccess(false);
        vo.setMsg(ex.getMessage());
        return vo;
    }

}
