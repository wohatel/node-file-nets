package com.murong.nets.util;

import eu.bitwalker.useragentutils.UserAgent;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


/**
 * description
 *
 * @author wangxiao 2024/03/27 10:55
 */
@SuppressWarnings("all")

@NoArgsConstructor
public class UserAgentUtil {

    /**
     * 获取osName
     *
     * @return
     * @author yaochuang 2024-05-16 18:35
     */
    public static String getOsName() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String ua = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgent.parseUserAgentString(ua);
            return userAgent.getOperatingSystem().getName();
        } catch (Exception var4) {
            return "";
        }
    }

    /**
     * 获取浏览器名称
     *
     * @return
     * @author yaochuang 2024-05-16 18:35
     */
    public static String getBrowser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String ua = request.getHeader("User-Agent");
            UserAgent userAgent = UserAgent.parseUserAgentString(ua);
            return userAgent.getBrowser().getName();
        } catch (Exception var4) {
            return "";
        }
    }

    /**
     * @param request 请求http
     * @return ip地址
     * @author yaochuang 2024-05-13 17:42
     */
    public static String getIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }
        return ipAddress;
    }
}
