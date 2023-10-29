package com.murong.rpc.config;

import com.murong.rpc.vo.NodeVo;

import java.util.ArrayList;
import java.util.List;

/**
 * rpc的一些配置
 */
public class EnvConfig {

    /**
     * 配置homeDirs ,磁盘上允许的dir操作路径
     */
    private static final List<String> homeDirs = new ArrayList<>();

    private static final List<NodeVo> centerNodes = new ArrayList<>();

    /**
     * 获取dirs路径
     *
     * @return
     */
    public static List<String> homeDirs() {
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
     * 添加dir路径
     *
     * @param homeDir
     * @return
     */
    public static List<String> addHomeDir(String homeDir) {
        homeDirs.add(homeDir);
        return homeDirs;
    }

    /**
     * 清理并添加dir路径,线程间要隔离
     *
     * @param homeDirs
     * @return
     */
    public static synchronized List<String> clearHomeDirsAndAddAll(List<String> homeDirs) {
        if (homeDirs != null) {
            EnvConfig.homeDirs.clear();
            EnvConfig.homeDirs.addAll(homeDirs);
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
        for (int i = 0; i < homeDirs.size(); i++) {
            boolean b = file.startsWith(homeDirs.get(i));
            if (b) {
                return true;
            }
        }
        return false;
    }

}
