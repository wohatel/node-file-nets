package com.murong.rpc.config;


import com.murong.rpc.vo.ResultVo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;

import java.util.Map;


/**
 * aop处理
 */
@Aspect
@Component
public class Aspects {

    Logger logger = LoggerFactory.getLogger(Aspects.class);

    /**
     * 以自定义 @LogPrint 注解为切点
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)||@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void logPrint() {
        // 我就一个切入点,为啥非要写个注释?
    }


    /**
     * 环绕
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("logPrint()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = proceedingJoinPoint.getArgs();
        Method method = signature.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();
        String clazzName = declaringClass.getName();
        String methodName = method.getName();

        if (parameterNames != null && parameterNames.length != 0) {
            // 打印参数
            Map<String, Object> paramMap = new HashMap<>();
            for (int i = 0; i < parameterNames.length; i++) {
                paramMap.put(parameterNames[i], args[i]);
            }
            logger.info("{}.{}请求参数:{}", clazzName, methodName, paramMap);
        }

        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        logger.info("接口耗时统计: {}.{}耗时:{} ms", clazzName, methodName, System.currentTimeMillis() - startTime);
        return result;
    }

}
