package com.murong.rpc.input;

public class RenameFileInput {

    /**
     * 源节点
     */
    private String nodeName;

    /**
     * 源文件
     */
    private String file;

    /**
     * 目标文件
     */
    private String newName;

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

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
