package com.murong.nets.service;

import com.murong.nets.client.ClientSitePool;
import com.murong.nets.client.RpcAutoReconnectClient;
import com.murong.nets.client.RpcDefaultClient;
import com.murong.nets.config.EnvConfig;
import com.murong.nets.config.ExecutorPool;
import com.murong.nets.config.NodeConfig;
import com.murong.nets.constant.RequestTypeEnmu;
import com.murong.nets.input.RenameFileInput;
import com.murong.nets.interaction.RpcFuture;
import com.murong.nets.interaction.RpcRequest;
import com.murong.nets.interaction.RpcResponse;
import com.murong.nets.util.JsonUtil;
import com.murong.nets.util.RpcException;
import com.murong.nets.util.RpcResponseHandler;
import com.murong.nets.util.StringUtil;
import com.murong.nets.util.ThreadUtil;
import com.murong.nets.vo.CpuUsageVo;
import com.murong.nets.vo.DirsVo;
import com.murong.nets.vo.EnvConfVo;
import com.murong.nets.vo.FileVo;
import com.murong.nets.vo.HardUsageVo;
import com.murong.nets.vo.MemoryUsageVo;
import com.murong.nets.vo.NodeVo;
import com.murong.nets.vo.OperateSystemVo;
import com.murong.nets.vo.ProcessActiveVo;
import com.murong.nets.vo.RateLimitVo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NodeService {

    Logger logger = LoggerFactory.getLogger(NodeService.class);


    private final NodeConfig nodeConfig;

    /**
     * 从注册节点获取所有的节点
     */
    @SneakyThrows
    public List<NodeVo> nodeList() {
        List<NodeVo> nodeVos = EnvConfig.getCenterNodes();
        if (CollectionUtils.isEmpty(nodeVos)) {
            throw new RpcException("未查询到中心节点");
        }
        RpcAutoReconnectClient client = ClientSitePool.getCenterClient();
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(RequestTypeEnmu.getNodes.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> JsonUtil.parseArray(t, NodeVo.class));
    }

    /**
     * 拷贝文件
     *
     * @param sourceNode 源节点
     * @param targetNode 目标节点
     * @param sourceFile 源文件全路径
     * @param targetFile 目标文件全路径
     */
    public boolean cpFile(String sourceNode, String targetNode, String sourceFile, String targetFile) {
        // 如果本机是来源节点
        if (sourceNode.equals(targetNode) && sourceFile.equals(targetFile)) {
            throw new RpcException("来源节点和目标节点一致且来源文件和目标文件一样,不需要拷贝");
        }
        if (sourceNode.equals(nodeConfig.getLocalNodeName())) {
            RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(targetNode);
            ExecutorPool.getExecutorService().submit(() -> {
                try {
                    client.sendFile(sourceFile, targetFile);
                } catch (IOException e) {
                    throw new RpcException(e);
                }
            });
            return true;
        } else {
            RpcDefaultClient rpcDefaultClient = ClientSitePool.getOrConnectClient(sourceNode);
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
     * @param sourceNode 源节点
     * @param targetNode 目标节点
     * @param sourceDir  源节点文件夹
     * @param targetDir  目标节点文件夹
     *                   响应结果
     */
    public boolean cpDir(String sourceNode, String targetNode, String sourceDir, String targetDir) {
        if (!sourceDir.endsWith("/")) {
            sourceDir += "/";
        }
        if (!targetDir.endsWith("/")) {
            targetDir += "/";
        }
        if (sourceNode.equals(targetNode)) {
            if (sourceDir.equals(targetDir)) {
                throw new RpcException("来源节点和目标节点一致且来源文件和目标文件一样,不需要拷贝");
            }
            if (targetDir.startsWith(sourceDir)) {
                throw new RpcException("父目录不可向子目录拷贝");
            }
        }
        // 如果本机是来源节点
        if (sourceNode.equals(nodeConfig.getLocalNodeName())) {
            RpcDefaultClient rpcDefaultClient = ClientSitePool.getOrConnectClient(targetNode);
            final String sourceDirF = sourceDir;
            final String targetDirF = targetDir;
            ExecutorPool.getExecutorService().submit(() -> {
                try {
                    rpcDefaultClient.sendDir(sourceDirF, targetDirF, 64 * 1024);
                } catch (Exception e) {
                    throw new RpcException(e);
                }
            });
            return true;
        } else {
            RpcDefaultClient rpcDefaultClient = ClientSitePool.getOrConnectClient(sourceNode);
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
     * @param sourceNode 源节点
     * @param file       文件全路径
     *                   文件信息
     */
    @SneakyThrows
    public FileVo fileInfo(String sourceNode, String file) {
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
     * @param sourceNode 节点名
     * @param dir        文件节
     */
    @SneakyThrows
    public List<FileVo> filesOfDir(String sourceNode, String dir) {
        if (StringUtil.isBlank(dir)) {
            throw new RpcException("文件名称为空");
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
     * @param sourceNode 节点名
     * @param file       文件全路径
     */
    @SneakyThrows
    public boolean fileDelete(String sourceNode, String file) {
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(sourceNode);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setBody(file);
        rpcRequest.setRequestType(RequestTypeEnmu.fileDelete.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        boolean aBoolean = Boolean.parseBoolean(rpcResponse.getBody());
        if (!aBoolean) {
            throw new RpcException(rpcResponse.getMsg());
        }
        return aBoolean;
    }

    /**
     * 向注册中心注册节点
     */
    public void registerNode() {
        // 中心节点
        List<NodeVo> centerNodeVos = EnvConfig.getCenterNodes();
        String format = "本机%s:%s向中心节点%s:%s注册";
        if (!CollectionUtils.isEmpty(centerNodeVos)) {
            for (NodeVo nodeVo : centerNodeVos) {
                String name = nodeVo.getName();

                NodeVo localNode = new NodeVo();
                localNode.setName(nodeConfig.getLocalNodeName());
                localNode.setPort(nodeConfig.getLocalNodePort());
                localNode.setHost(nodeConfig.getLocalNodeHost());
                localNode.setStartTime(nodeConfig.getStartTime());

                ThreadUtil.execSilentVoid(() -> {
                    logger.info(String.format(format, nodeConfig.getLocalNodeHost(), nodeConfig.getLocalNodePort(), nodeVo.getHost(), nodeVo.getPort()));
                    RpcDefaultClient orConnectClient = ClientSitePool.getOrConnectClient(name);
                    RpcRequest request = new RpcRequest();
                    request.setRequestType(RequestTypeEnmu.registerNode.name());
                    request.setBody(JsonUtil.toJSONString(localNode)); // 将本地节点注册给中心节点
                    orConnectClient.sendMsg(request);
                });

            }
        }
    }

    /**
     * 变更工作目录
     *
     * @param dirs 新的工作目录
     */
    @SneakyThrows
    public boolean chHomeDirs(List<String> dirs) {
        RpcAutoReconnectClient client = ClientSitePool.getCenterClient();
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setBody(JsonUtil.toJSONString(dirs));
        rpcRequest.setRequestType(RequestTypeEnmu.chHomeDirs.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, Boolean::valueOf);
    }

    /**
     * 尝试建立center链接
     */
    public void acceptCenter() {
        List<NodeVo> nodeVos = EnvConfig.getCenterNodes();
        if (!CollectionUtils.isEmpty(nodeVos)) {
            for (NodeVo nodeVo : nodeVos) {
                ThreadUtil.execSilentException(() -> ClientSitePool.accept(nodeVo), e -> logger.error("acceptCenter", e));
            }
        }
    }

    /**
     * 查询节点的连接情况
     */
    @SneakyThrows
    public List<NodeVo> linkedList(String nodeName) {
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
     * @param nodeName 节点名称
     * @param file     文件全限定名
     * @param newName  文件新名称
     */
    @SneakyThrows
    public boolean renameFile(String nodeName, String file, String newName) {
        RpcAutoReconnectClient orConnectClient = ClientSitePool.getOrConnectClient(nodeName);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(RequestTypeEnmu.renameFile.name());

        RenameFileInput input = new RenameFileInput();
        input.setFile(file);
        input.setNewName(newName);

        rpcRequest.setBody(JsonUtil.toJSONString(input));
        RpcFuture rpcFuture = orConnectClient.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, Boolean::valueOf);
    }

    /**
     * 中心节点之间配置信息及节点信息
     * 2: 家目录
     * 3: 限速策略
     */
    public void syncCenterConf() {
        List<NodeVo> nodeVos = EnvConfig.getCenterNodes(); //所有的中心节点,包括自己
        List<String> nodeNames = nodeVos.stream().map(NodeVo::getName).collect(Collectors.toList());
        if (!nodeNames.contains(nodeConfig.getLocalNodeName())) { // 如果不是中心节点,不需要同步
            return;
        }
        for (NodeVo nodeVo : nodeVos) {
            ThreadUtil.execSilentException(() -> {
                RpcAutoReconnectClient client = ClientSitePool.get(nodeVo.getName());
                if (client != null) {
                    RpcRequest rpcRequest = new RpcRequest();
                    rpcRequest.setRequestType(RequestTypeEnmu.getConf.name());
                    RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
                    RpcResponse rpcResponse = rpcFuture.get();
                    EnvConfVo confVo = JsonUtil.parseObject(rpcResponse.getBody(), EnvConfVo.class);
                    if (confVo != null) {
                        // 同步家目录信息
                        DirsVo dirsVo = confVo.getDirsVo();
                        if (!CollectionUtils.isEmpty(dirsVo.getDirs())) {// 如果非空
                            EnvConfig.clearHomeDirsAndAddAll(dirsVo.getDirs(), dirsVo.getTime());
                        }
                        // 同步限速策略
                        RateLimitVo rateLimitVo = confVo.getRateLimitVo();
                        if (rateLimitVo != null) {// 如果非空
                            EnvConfig.casRateLimit(rateLimitVo.getRateLimit(), rateLimitVo.getTime());
                        }
                    }
                }
            }, e -> logger.error("syncCenterConf:", e));
        }
    }

    /**
     * 普通节点从中心节点拉取配置
     * 2: 家目录
     * 3: 限速策略
     */
    @SneakyThrows
    public void syncConf() {
        List<NodeVo> nodeVos = EnvConfig.getCenterNodes();
        List<String> nodeNames = nodeVos.stream().map(NodeVo::getName).toList();
        if (nodeNames.contains(nodeConfig.getLocalNodeName())) { // 如果自己是中心节点直接不需要再获取
            return;
        }
        RpcAutoReconnectClient client = ClientSitePool.getCenterClient();
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(RequestTypeEnmu.getConf.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        EnvConfVo confVo = JsonUtil.parseObject(rpcResponse.getBody(), EnvConfVo.class);
        if (confVo != null) {
            // 同步家目录信息
            DirsVo dirsVo = confVo.getDirsVo();
            if (!CollectionUtils.isEmpty(dirsVo.getDirs())) {// 如果非空
                EnvConfig.clearHomeDirsAndAddAll(dirsVo.getDirs(), dirsVo.getTime());
            }
            // 同步限速策略
            RateLimitVo rateLimitVo = confVo.getRateLimitVo();
            if (rateLimitVo != null) {// 如果非空
                EnvConfig.casRateLimit(rateLimitVo.getRateLimit(), rateLimitVo.getTime());
            }
        }

    }


    /**
     * 限速命令
     * kb/s
     */
    @SneakyThrows
    public Boolean chRateLimit(long rateLimit) {
        RpcAutoReconnectClient client = ClientSitePool.getCenterClient();
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setBody(String.valueOf(rateLimit));
        rpcRequest.setRequestType(RequestTypeEnmu.chRateLimit.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, Boolean::valueOf);
    }

    /**
     * 获取cpu的使用情况
     *
     * @param nodeName 节点名称
     */
    @SneakyThrows
    public CpuUsageVo cpuUsage(String nodeName) {
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(nodeName);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(RequestTypeEnmu.cpuUsage.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> JsonUtil.parseObject(t, CpuUsageVo.class));
    }

    @SneakyThrows
    public MemoryUsageVo memoryUsage(String nodeName) {
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(nodeName);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(RequestTypeEnmu.memoryUsage.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> JsonUtil.parseObject(t, MemoryUsageVo.class));
    }

    @SneakyThrows
    public List<HardUsageVo> hardUsage(String nodeName) {
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(nodeName);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(RequestTypeEnmu.hardUsage.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> JsonUtil.parseArray(t, HardUsageVo.class));
    }

    @SneakyThrows
    public List<ProcessActiveVo> processList(String nodeName, int topNumber) {
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(nodeName);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setBody(String.valueOf(topNumber));
        rpcRequest.setRequestType(RequestTypeEnmu.processList.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> JsonUtil.parseArray(t, ProcessActiveVo.class));
    }

    /**
     * 判断文件是否正在接收
     *
     * @param targetNode 目标节点
     * @param targetFile 目标文件
     */
    @SneakyThrows
    public boolean operabitilyCheck(String targetNode, String targetFile) {
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(targetNode);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setBody(targetFile);
        rpcRequest.setRequestType(RequestTypeEnmu.operabitilyCheck.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, Boolean::valueOf);
    }

    /**
     * 清理ok文件
     *
     * @param targetNode 目标节点
     * @param targetFile 目标文件
     */
    public Boolean clearOk(String targetNode, String targetFile) {
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(targetNode);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setBody(targetFile);
        rpcRequest.setRequestType(RequestTypeEnmu.clearOk.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, Boolean::valueOf);
    }

    /**
     * 获取操作系统信息
     *
     * @param targetNode 目标节点
     */
    public OperateSystemVo operateSystemInfo(String targetNode) {
        RpcAutoReconnectClient client = ClientSitePool.getOrConnectClient(targetNode);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestType(RequestTypeEnmu.operateSystemInfo.name());
        RpcFuture rpcFuture = client.sendSynMsg(rpcRequest);
        RpcResponse rpcResponse = rpcFuture.get();
        return RpcResponseHandler.handler(rpcResponse, t -> JsonUtil.parseObject(t, OperateSystemVo.class));
    }
}
