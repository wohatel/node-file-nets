package com.murong.rpc.controller;

import com.murong.rpc.vo.ResultVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;


/**
 * @author yaochuang
 * @ClassName AuthUserController
 * @Desription TODO
 * @Date 2023/5/6 5:00 PM
 * @Version 1.0
 */
@RestController
public class SecurityController {

    /**
     * 查询所有的节点
     */
    @GetMapping("/user/initAdminPassword")
    public ResultVo<Boolean> initAdminPassword() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return null;
    }

}
