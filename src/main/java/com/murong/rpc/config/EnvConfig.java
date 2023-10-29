package com.murong.rpc.config;

import com.murong.rpc.util.KeyValue;
import com.murong.rpc.vo.DirsVo;
import com.murong.rpc.vo.NodeVo;
import lombok.Data;
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

    private static final List<NodeVo> centerNodes = new ArrayList<>();

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
            if (!CollectionUtils.isEmpty(homeDirs)) {
                EnvConfig.homeDirs.getDirs().addAll(homeDirs);
            }
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
            boolean b = file.startsWith(homeDirs.getDirs().get(i));
            if (b) {
                return true;
            }
        }
        return false;
    }

}
