package com.murong.nets.vo;

import com.murong.nets.constant.LimitMode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class WebServiceStatusVo {

    private List<String> clients = new ArrayList<>();

    private LimitMode limitMode = LimitMode.all_open;

    private long time;
}
