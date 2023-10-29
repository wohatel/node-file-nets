package com.murong.rpc.vo;

public class FileVo {

    private String name;
    private String path;
    private Long length;

    private boolean dictionary;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public boolean isDictionary() {
        return dictionary;
    }

    public void setDictionary(boolean dictionary) {
        this.dictionary = dictionary;
    }
}
