package com.murong.rpc.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DirsVo {

    private List<String> dirs = new ArrayList<>();

    private long time;
}
