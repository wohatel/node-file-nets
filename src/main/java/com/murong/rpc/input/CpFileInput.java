package com.murong.rpc.input;

public class CpFileInput {

    /**
     * 源节点
     */
    private String sourceNode;

    /**
     * 目标
     */
    private String targetNode;

    /**
     * 源文件
     */
    private String sourceFile;

    /**
     * 目标文件
     */
    private String targetFile;

    public String getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(String sourceNode) {
        this.sourceNode = sourceNode;
    }

    public String getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(String targetNode) {
        this.targetNode = targetNode;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }
}
