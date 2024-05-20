package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 开启一些节点的web服务
 *
 * @author yaochuang 2024/05/20 11:33
 */
@Data
public class WebSericeOpenInput {

    /**
     * 节点名称
     */
    @NotEmpty(message = "nodeNames 参数不能为空集合")
    @Size(max = 300)
    private List<String> nodeNames;
}
