package com.murong.rpc.constant;

import javax.annotation.Resource;

/**
 * tag的分类
 */
public enum RequestTypeEnmu {
    /**
     * 传输文件指令
     */
    sendFile,

    /**
     * 传送文件夹
     */
    sendDir,


    /**
     * 获取nodes
     */
    getNodes,


    /**
     * 注册节点
     */
    registerNode,


    /**
     * 获取单个节点
     */
    getNode,

    /**
     * 获取文件信息
     */
    fileInfo,

    /**
     * 获取文件信息
     */
    filesOfDir,

    /**
     * 文件的删除
     */
    fileDelete,

    /**
     * 变更工作目录
     */
    chHomeDirs,

    /**
     * 获取工作目录
     */
    getHomeDirs,

    /**
     * 查询节点连接情况
     */
    linkedList,
    /**
     * 文件重命名
     */
    renameFile;
}
