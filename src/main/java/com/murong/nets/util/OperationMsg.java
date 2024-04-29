package com.murong.nets.util;

import lombok.Data;

/**
 * 操作结果
 */
@Data
public class OperationMsg {

    private boolean operateStatus = true;

    private String msg;

    private int code;
}
