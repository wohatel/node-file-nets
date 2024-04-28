package com.murong.rpc.controller;

import com.murong.rpc.vo.ResultVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>
 * 涉及安全的集群处理 TODO 暂时未实现具体功能
 * </p>
 *
 * @author yaochuang 2024/04/28 17:11
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
