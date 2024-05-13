package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class DownlineNodeInput {

    /**
     * 节点名称
     */
    @NotEmpty(message = "nodeNames 参数不能为空集合")
    private List<String> nodeNames;

}
