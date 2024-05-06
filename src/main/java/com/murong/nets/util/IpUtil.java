package com.murong.nets.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * 自动查询ip地址
 *
 * @author yaochuang 2024/05/06 16:14
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IpUtil {

    /**
     * 获取内网ip
     *
     * @return String ip
     * @author yaochuang 2024-05-06 16:16
     */
    @SneakyThrows
    public static String getIp() {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        Iterator<NetworkInterface> iterator = interfaces.asIterator();
        while (iterator.hasNext()) {
            NetworkInterface next = iterator.next();
            if (isSimpleEt(next)) {
                Enumeration<InetAddress> addresses = next.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 排除 IPv6 地址和链接本地地址
                    if (addr.isLinkLocalAddress() || addr.isLoopbackAddress()) {
                        continue;
                    }
                    String hostAddress = addr.getHostAddress();
                    if (hostAddress.contains(".")) {
                        return hostAddress;
                    }
                }

            }
        }
        return null;
    }

    @SneakyThrows
    private static boolean isSimpleEt(NetworkInterface networkInterface) {
        if (networkInterface.isVirtual()) {
            return false;
        }
        if (!networkInterface.isUp()) {
            return false;
        }
        String name = networkInterface.getName();
        String regx = "\\d+";
        if (name.startsWith("en")) {
            String num = name.substring(2);
            if (num.matches(regx)) {
                return true;
            }
        }
        if (name.startsWith("eth")) {
            String num = name.substring(3);
            return num.matches(regx);
        }
        return false;
    }
}
