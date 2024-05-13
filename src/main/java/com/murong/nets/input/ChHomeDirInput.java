package com.murong.nets.input;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class ChHomeDirInput {

    /**
     * 源节点
     */
    @NotEmpty
    private List<String> homeDirs;
}
