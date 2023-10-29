package com.murong.rpc.input;

/**
 * 获取节点的文件信息
 */
public class DelFileOrDirInput {

    /**
     * 源节点
     */
    private String nodeName;

    /**
     * 目标文件或文件夹
     */
    private String fileOrDir;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getFileOrDir() {
        return fileOrDir;
    }

    public void setFileOrDir(String fileOrDir) {
        this.fileOrDir = fileOrDir;
    }
}
