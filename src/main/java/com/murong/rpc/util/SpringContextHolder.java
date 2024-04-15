package com.murong.rpc.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * spring上下文holder implements应用上下文aware,可任意处理的bean
 */
@Slf4j
@Component
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {
    /**
     * 上下文
     */
    @Getter
    private static ApplicationContext context;
    /**
     * 日志
     */
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * injectApplicationContext
     *
     * @param appcontext
     */
    public static void injectApplicationContext(ApplicationContext appcontext) {
        if (context == null) {
            context = appcontext;
        }
    }

    /**
     * 得到bean
     */
    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    /**
     * 得到bean
     */
    public static <T> T getBean(Class<T> clazzName) {
        return context.getBean(clazzName);
    }

    /**
     * 得到bean
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        return context.getBean(beanName, clazz);
    }

    /**
     * getContext
     *
     * @return
     */
    @Override
    public void setApplicationContext(ApplicationContext appcontext) throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("begin to initalize context!!!");
        }
        injectApplicationContext(appcontext);
        if (logger.isDebugEnabled()) {
            logger.debug("end to initalize context!!!");
        }
    }

    /**
     * 破坏
     */
    @Override
    public void destroy() {
        if (context.getClass().isAssignableFrom(ClassPathXmlApplicationContext.class)) {
            ((ClassPathXmlApplicationContext) context).close();
        } else if (context.getClass().isAssignableFrom(FileSystemXmlApplicationContext.class)) {
            ((FileSystemXmlApplicationContext) context).close();
        }
    }
}
