package com.murong.rpc.input;

/**
 * 获取节点的文件信息
 */
public class GetFileOfDirInput {

    /**
     * 源节点
     */
    private String nodeName;

    /**
     * 目标
     */
    private String dir;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}
