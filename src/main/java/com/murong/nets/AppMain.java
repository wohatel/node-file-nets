package com.murong.nets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 启动类
 */
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
public class AppMain {

    /**
     * 启动方法
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(AppMain.class, args);
    }

}
