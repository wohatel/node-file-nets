package com.murong.nets.config;

import com.murong.nets.vo.AuthenticationVo;
import com.murong.nets.vo.DirsVo;
import com.murong.nets.vo.NodeVo;
import com.murong.nets.vo.RateLimitVo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * rpc的一些配置
 */
@Data
public class EnvConfig {

    /**
     * 配置homeDirs ,磁盘上允许的dir操作路径
     */
    private static final DirsVo homeDirs = new DirsVo();

    /**
     * 限速策略
     */
    @Getter
    private static final RateLimitVo rateLimitVo = new RateLimitVo();

    /**
     * 集群的认证
     */
    @Getter
    private static final AuthenticationVo authenticationVo = new AuthenticationVo();

    /**
     * 节点服务是否可用
     */
    @Getter
    private static final AtomicBoolean serviceAvailable = new AtomicBoolean(true);

    /**
     * 中心节点
     */
    @Getter
    private static final List<NodeVo> centerNodes = new ArrayList<>();


    @Getter
    @Setter
    private static String localNodeName;

    /**
     * 获取dirs路径
     */
    public static DirsVo homeDirs() {
        return homeDirs;
    }

    /**
     * 清理并添加dir路径,线程间要隔离
     */
    public static synchronized void clearHomeDirsAndAddAll(List<String> homeDirs, Long time) {
        Long localTime = EnvConfig.homeDirs.getTime();
        if (time > localTime) {
            EnvConfig.homeDirs.getDirs().clear();
            EnvConfig.homeDirs.getDirs().addAll(homeDirs);
            EnvConfig.homeDirs.setTime(time);
        }
    }

    /**
     * 添加nodeName路径
     */
    public static void addCenterNode(NodeVo nodeVo) {
        centerNodes.add(nodeVo);
    }

    /**
     * 判断路径是否在工作目录中
     */
    public static boolean isFilePathOk(String file) {
        if (file == null) {
            return false;
        }
        for (int i = 0; i < homeDirs.getDirs().size(); i++) {
            String dir = homeDirs.getDirs().get(i);
            if (dir.endsWith("/")) {
                dir = dir.substring(0, dir.length() - 1);
            }
            boolean b = file.startsWith(dir);
            if (b) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置限速
     */
    public static void casRateLimit(long rateLimit, long time) {
        if (time > rateLimitVo.getTime()) {
            rateLimitVo.setRateLimit(rateLimit);
            rateLimitVo.setTime(time);
        }
    }


    /**
     * 判断是否是中心节点
     */
    public static boolean isCenterNode() {
        return centerNodes.stream().anyMatch(t -> t.getName().equals(localNodeName));
    }

    /**
     * 服务已过期
     */
    public static boolean isTokeExpired() {
        LocalDateTime expireTime = authenticationVo.getExpireTime();
        if (LocalDateTime.now().isAfter(expireTime)) {
            return true;
        }
        return false;
    }

    /**
     * 超出节点数
     */
    public static boolean isServiceAvailable() {

        LocalDateTime expireTime = authenticationVo.getExpireTime();
        if (LocalDateTime.now().isAfter(expireTime)) {
            return true;
        }
        return false;
    }

}
