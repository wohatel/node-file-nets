package com.murong.rpc.config;

import com.murong.rpc.vo.DirsVo;
import com.murong.rpc.vo.NodeVo;
import com.murong.rpc.vo.RateLimitVo;
import lombok.Data;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

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
    private static final RateLimitVo rateLimitVo = new RateLimitVo();


    private static final List<NodeVo> centerNodes = new ArrayList<>();

    private static String localNodeName;

    public static String getLocalNodeName() {
        return localNodeName;
    }

    public static void setLocalNodeName(String localNodeName) {
        EnvConfig.localNodeName = localNodeName;
    }

    /**
     * 获取dirs路径
     *
     * @return
     */
    public static DirsVo homeDirs() {
        return homeDirs;
    }

    /**
     * 获取中心节点名称
     *
     * @return
     */
    public static List<NodeVo> centerNodes() {
        return centerNodes;
    }

    /**
     * 清理并添加dir路径,线程间要隔离
     *
     * @param homeDirs
     * @return
     */
    public static synchronized List<String> clearHomeDirsAndAddAll(List<String> homeDirs, Long time) {
        Long localTime = EnvConfig.homeDirs.getTime();
        if (time > localTime) {
            EnvConfig.homeDirs.getDirs().clear();
            EnvConfig.homeDirs.getDirs().addAll(homeDirs);
            EnvConfig.homeDirs.setTime(time);
        }
        return homeDirs;
    }

    /**
     * 添加nodeName路径
     *
     * @param nodeVo
     * @return
     */
    public static List<NodeVo> addCenterNode(NodeVo nodeVo) {
        centerNodes.add(nodeVo);
        return centerNodes;
    }

    /**
     * 判断路径是否在工作目录中
     *
     * @param file
     * @return
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
     *
     * @param rateLimit
     * @param time
     */
    public static void casRateLimit(long rateLimit, long time) {
        if (time > rateLimitVo.getTime()) {
            rateLimitVo.setRateLimit(rateLimit);
            rateLimitVo.setTime(time);
        }
    }


    public static RateLimitVo getRateLimitVo() {
        return rateLimitVo;
    }
}
