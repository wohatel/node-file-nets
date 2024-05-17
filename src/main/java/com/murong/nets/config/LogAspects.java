package com.murong.nets.config;


import com.alibaba.fastjson2.JSON;
import com.murong.nets.constant.LogRecord;
import com.murong.nets.util.ThreadUtil;
import com.murong.nets.util.UserAgentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;


/**
 * 权限拦截器方法过滤
 *
 * @author yaochuang 2024/03/28 11:18
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspects {

    /**
     * 以自定义 @LogPrint 注解为切点
     */
    @Pointcut("execution(public * com.murong.nets.controller..*.*(..))")
    public void log() {
    }

    /**
     * 登录验证环通知
     *
     * @param joinPoint 切入端
     * @return Object
     */
    @Around("log()")
    public Object doAroundLog(ProceedingJoinPoint joinPoint) throws Throwable {
        ThreadUtil.execSilentVoid(() -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert attributes != null;
            Object[] args = joinPoint.getArgs();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String perFunc = method.getDeclaringClass().getName() + "." + method.getName();
            LogRecord logRecord = new LogRecord(UserAgentUtil.getIp(), UserAgentUtil.getOsName(), UserAgentUtil.getBrowser(), perFunc, getParamsAsString(args), LocalDateTime.now());
            log.info("web请求node:{} and 请求详情:{}", EnvConfig.getLocalNodeName(), logRecord);
        });
        return joinPoint.proceed();
    }


    public static String getParamsAsString(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        // 排除特殊类型的参数，如文件类型
        Object[] arguments = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof ServletRequest || args[i] instanceof ServletResponse || args[i] instanceof MultipartFile) {
                continue;
            }
            arguments[i] = args[i];
        }
        return JSON.toJSONString(arguments);
    }

}
