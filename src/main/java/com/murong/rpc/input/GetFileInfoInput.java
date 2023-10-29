package com.murong.rpc.input;

/**
 * 获取节点的文件信息
 */
public class GetFileInfoInput {

    /**
     * 源节点
     */
    private String nodeName;

    /**
     * 目标
     */
    private String file;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
