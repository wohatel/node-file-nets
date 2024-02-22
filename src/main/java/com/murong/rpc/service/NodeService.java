package com.murong.rpc.service;

import com.murong.rpc.client.ClientSitePool;
import com.murong.rpc.client.RpcAutoReconnectClient;
import com.murong.rpc.client.RpcDefaultClient;
import com.murong.rpc.config.EnvConfig;
import com.murong.rpc.config.ExecutorPool;
import com.murong.rpc.config.NodeConfig;
import com.murong.rpc.constant.RequestTypeEnmu;
import com.murong.rpc.input.RenameFileInput;
import com.murong.rpc.interaction.RpcFuture;
import com.murong.rpc.interaction.RpcRequest;
import com.murong.rpc.interaction.RpcResponse;
import com.murong.rpc.util.JsonUtil;
import com.murong.rpc.util.RpcResponseHandler;
import com.murong.rpc.util.StringUtil;
import com.murong.rpc.util.ThreadUtil;
import com.murong.rpc.vo.DirsVo;
import com.murong.rpc.vo.FileVo;
import com.murong.rpc.vo.NodeVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class NodeService {

    Logger logger = LoggerFactory.getLogger(NodeService.class);

    @Autowired
    NodeConfig nodeConfig;

    /**
     * 从注册节点获取所有的节点
     *
     * @return
     */
    public List<NodeVo> nodeList() throws InterruptedException {
        List<NodeVo> nodeVos = EnvConfig.centerNodes();
        if (CollectionUtils.isEmpty(nodeVos)) {
            return new ArrayList<>();
        }
        RpcAutoReconnectClient client = ClientSitePool.getCenterClient();
        if (client == null) {
            return new ArrayList<>();
        }
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(RequestTypeEnmu.getNodes.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> JsonUtil.parseArray(t, NodeVo.class));
    }

    /**
     * 拷贝文件
     *
     * @param sourceNode
     * @param targetNode
     * @param sourceFile
     * @param targetFile
     * @return
     */
    public boolean cpFile(String sourceNode, String targetNode, String sourceFile, String targetFile) {
        // 如果本机是来源节点
        if (sourceNode.equals(targetNode) && sourceFile.equals(targetFile)) {
            throw new RuntimeException("来源节点和目标节点一致且来源文件和目标文件一样,不需要拷贝");
        }
        if (sourceNode.equals(nodeConfig.getLocalNodeName())) {
            RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(targetNode);
            if (client == null) {
                throw new RuntimeException("无指向目标节点的");
            }
            ExecutorPool.getExecutorService().submit(() -> {
                try {
                    client.sendFile(sourceFile, targetFile);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return true;
        } else {
            RpcDefaultClient rpcDefaultClient = ClientSitePool.getOrConnectClient(sourceNode);
            if (rpcDefaultClient == null) {
                throw new RuntimeException("未发现源节点的连接");
            }
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setRequestType(RequestTypeEnmu.sendFile.name());
            List<String> body = new ArrayList<>(); // 此body是需要告诉sourceNode
            body.add(targetNode);// 传输文件到targetNode
            body.add(sourceFile);// sourceNode的文件
            body.add(targetFile);// targetNode的文件
            rpcRequest.setBody(JsonUtil.toJSONString(body));
            rpcDefaultClient.sendMsg(rpcRequest);
            return true;
        }
    }

    /**
     * 拷贝目录
     *
     * @param sourceNode
     * @param targetNode
     * @param sourceDir
     * @param targetDir
     * @return
     */
    public boolean cpDir(String sourceNode, String targetNode, String sourceDir, String targetDir) throws InterruptedException {
        if (!sourceDir.endsWith("/")) {
            sourceDir += "/";
        }
        if (!targetDir.endsWith("/")) {
            targetDir += "/";
        }
        if (sourceNode.equals(targetNode)) {
            if (sourceDir.equals(targetDir)) {
                throw new RuntimeException("来源节点和目标节点一致且来源文件和目标文件一样,不需要拷贝");
            }
            if (targetDir.startsWith(sourceDir)) {
                throw new RuntimeException("父目录不可向子目录拷贝");
            }
        }
        // 如果本机是来源节点
        if (sourceNode.equals(nodeConfig.getLocalNodeName())) {
            RpcDefaultClient rpcDefaultClient = ClientSitePool.getOrConnectClient(targetNode);
            if (rpcDefaultClient == null) {
                throw new RuntimeException("未发现目标节点的连接:" + targetNode);
            }
            final String sourceDirF = sourceDir;
            final String targetDirF = targetDir;
            ExecutorPool.getExecutorService().submit(() -> {
                try {
                    rpcDefaultClient.sendDir(sourceDirF, targetDirF, 1024 * 1024);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return true;
        } else {
            RpcDefaultClient rpcDefaultClient = ClientSitePool.getOrConnectClient(sourceNode);
            if (rpcDefaultClient == null) {
                throw new RuntimeException("无指向来源节点的连接");
            }
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setRequestType(RequestTypeEnmu.sendDir.name());
            List<String> body = new ArrayList<>(); // 此body是需要告诉sourceNode
            body.add(targetNode);// 传输文件到targetNode
            body.add(sourceDir);// sourceNode的文件夹
            body.add(targetDir);// targetNode的文件夹
            rpcRequest.setBody(JsonUtil.toJSONString(body));
            rpcDefaultClient.sendMsg(rpcRequest);
            return true;
        }
    }

    /**
     * 查询文件内容
     *
     * @param sourceNode
     * @param file
     * @return
     */
    public FileVo fileInfo(String sourceNode, String file) throws InterruptedException {
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(sourceNode);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setBody(file);
        rpcRequest.setRequestType(RequestTypeEnmu.fileInfo.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> JsonUtil.parseObject(t, FileVo.class));
    }

    /**
     * 查询某节点下的所有文件信息
     *
     * @param sourceNode
     * @param dir
     * @return
     */
    public List<FileVo> filesOfDir(String sourceNode, String dir) throws InterruptedException {
        if (StringUtil.isBlank(dir)) {
            throw new RuntimeException("文件名称为空");
        }
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(sourceNode);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setBody(dir);
        rpcRequest.setRequestType(RequestTypeEnmu.fileInfo.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> JsonUtil.parseArray(t, FileVo.class));
    }

    /**
     * 删除文件或文件夹
     *
     * @param sourceNode
     * @param file
     * @return
     * @throws InterruptedException
     */
    public boolean fileDelete(String sourceNode, String file) throws InterruptedException {
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(sourceNode);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setBody(file);
        rpcRequest.setRequestType(RequestTypeEnmu.fileDelete.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        Boolean aBoolean = Boolean.valueOf(rpcResponse.getBody());
        if (!aBoolean) {
            throw new RuntimeException(rpcResponse.getMsg());
        }
        return aBoolean;
    }

    /**
     * 向注册中心注册节点
     */
    public void registerNode() {
        // 中心节点
        List<NodeVo> centerNodeVos = EnvConfig.centerNodes();
        String format = "本机%s:%s向中心节点%s:%s注册";
        if (!CollectionUtils.isEmpty(centerNodeVos)) {
            for (int i = 0; i < centerNodeVos.size(); i++) {
                NodeVo nodeVo = centerNodeVos.get(i);
                String name = nodeVo.getName();

                NodeVo localNode = new NodeVo();
                localNode.setName(nodeConfig.getLocalNodeName());
                localNode.setPort(nodeConfig.getNodePort());
                localNode.setHost(nodeConfig.getNodeHost());
                localNode.setStartTime(nodeConfig.getStartTime());

                ThreadUtil.execSilentVoid(() -> {
                    logger.info(String.format(format, nodeConfig.getNodeHost(), nodeConfig.getNodePort(), nodeVo.getHost(), nodeVo.getPort()));
                    RpcDefaultClient orConnectClient = ClientSitePool.getOrConnectClient(name);
                    if (orConnectClient != null) {
                        RpcRequest request = new RpcRequest();
                        request.setRequestType(RequestTypeEnmu.registerNode.name());
                        request.setBody(JsonUtil.toJSONString(localNode)); // 将本地节点注册给中心节点
                        orConnectClient.sendMsg(request);
                    }
                });

            }
        }
    }

    /**
     * 获取中心节点的工作目录并同步
     */
    public void syncCenterHomeDirs() throws InterruptedException {
        RpcAutoReconnectClient centerClient = ClientSitePool.getCenterClient();
        RpcRequest request = new RpcRequest();
        request.setRequestType(RequestTypeEnmu.getHomeDirs.name());
        RpcFuture rpcFuture = centerClient.sendSynMsg(request);
        RpcResponse rpcResponse = rpcFuture.get();
        logger.info("ack家目录:" + rpcResponse.getBody());
        DirsVo dirsVo = JsonUtil.parseObject(rpcResponse.getBody(), DirsVo.class);
        EnvConfig.clearHomeDirsAndAddAll(dirsVo.getDirs(), dirsVo.getTime());
    }

    /**
     * 变更工作目录
     *
     * @param dirs
     * @return
     */
    public boolean chHomeDirs(List<String> dirs) throws InterruptedException {
        RpcAutoReconnectClient client = ClientSitePool.getCenterClient();
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setBody(JsonUtil.toJSONString(dirs));
        rpcRequest.setRequestType(RequestTypeEnmu.chHomeDirs.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> Boolean.valueOf(t));
    }

    /**
     * 尝试建立center链接
     */
    public void acceptCenter() {
        List<NodeVo> nodeVos = EnvConfig.centerNodes();
        if (!CollectionUtils.isEmpty(nodeVos)) {
            for (NodeVo nodeVo : nodeVos) {
                ThreadUtil.execSilentException(() -> {
                    ThreadUtil.execSilentVoid(() -> {
                        ClientSitePool.accept(nodeVo);
                    });
                }, e -> e.printStackTrace());
            }
        }
    }

    /**
     * 查询节点的连接情况
     *
     * @return
     */
    public List<NodeVo> linkedList(String nodeName) throws InterruptedException {
        RpcAutoReconnectClient orConnectClient = ClientSitePool.getOrConnectClient(nodeName);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(RequestTypeEnmu.linkedList.name());
        rpcRequest.setBody(nodeName);
        RpcFuture rpcFuture = orConnectClient.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> JsonUtil.parseArray(t, NodeVo.class));
    }

    /**
     * 给文件重命名
     *
     * @param nodeName
     * @param file
     * @param newName
     * @param
     * @return
     */
    public boolean renameFile(String nodeName, String file, String newName) throws InterruptedException {
        RpcAutoReconnectClient orConnectClient = ClientSitePool.getOrConnectClient(nodeName);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(RequestTypeEnmu.renameFile.name());

        RenameFileInput input = new RenameFileInput();
        input.setFile(file);
        input.setNewName(newName);

        rpcRequest.setBody(JsonUtil.toJSONString(input));
        RpcFuture rpcFuture = orConnectClient.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> Boolean.valueOf(t));
    }

    /**
     * 中心节点之间同步节点的信息变化
     */
    public void syncCenterNodes() {
        List<NodeVo> nodeVos = EnvConfig.centerNodes();
        List<String> nodeNames = nodeVos.stream().map(t -> t.getName()).collect(Collectors.toList());
        if (!nodeNames.contains(nodeConfig.getNodeName())) { // 如果不是中心节点,不需要同步
            return;
        }
        Map<String, NodeVo> map = new ConcurrentHashMap<>();
        for (int i = 0; i < nodeVos.size(); i++) {
            NodeVo nodeVo = nodeVos.get(i);
            ThreadUtil.execSilentException(() -> {
                RpcAutoReconnectClient client = ClientSitePool.get(nodeVo.getName());
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setRequestType(RequestTypeEnmu.getNodes.name());
                RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
                RpcResponse rpcResponse = rpcFuture.get();
                List<NodeVo> allNodes = JsonUtil.parseArray(rpcResponse.getBody(), NodeVo.class);
                if (!CollectionUtils.isEmpty(allNodes)) {
                    for (NodeVo vo : allNodes) {
                        NodeVo nodeVo1 = map.get(vo.getName()); // 如果有变更时间,则给与优先
                        if (vo.getStartTime() > nodeVo1.getStartTime()) {
                            map.put(vo.getName(), vo);
                        }
                    }
                }
            }, e -> e.printStackTrace());
        }
    }
}
