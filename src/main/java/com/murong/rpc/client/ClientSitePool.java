package com.murong.rpc.client;

import com.murong.rpc.config.EnvConfig;
import com.murong.rpc.constant.RequestTypeEnmu;
import com.murong.rpc.interaction.RpcFuture;
import com.murong.rpc.interaction.RpcRequest;
import com.murong.rpc.interaction.RpcResponse;
import com.murong.rpc.util.JsonUtil;
import com.murong.rpc.util.KeyValue;
import com.murong.rpc.util.ThreadUtil;
import com.murong.rpc.vo.NodeVo;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSitePool {

    static Logger logger = LoggerFactory.getLogger(ClientSitePool.class);

    /**
     * 配置公用的eventLoopGroup
     */

    private static NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
    /**
     * 连接配置三个
     */
    private static int poolSize = 3;

    /**
     * 默认配置
     * 第二个long表示连接建立的时间
     * 第三个long表示最后一次使用时间
     */
    private static final Map<String, KeyValue<List<RpcAutoReconnectClient>, RpcHeartClient, NodeVo>> clientPool = new ConcurrentHashMap<>();

    /**
     * 接收配置
     */
    public static void accept(NodeVo nodeVo) {
        if (clientPool.containsKey(nodeVo.getName())) {
            if (EnvConfig.getLocalNodeName().equals(nodeVo.getName())) {// 说明是中心节点自己向自己注册
                clientPool.get(nodeVo.getName()).setData(nodeVo);
            }
            return;
        }
        List<RpcAutoReconnectClient> list = new ArrayList<>();
        KeyValue<List<RpcAutoReconnectClient>, RpcHeartClient, NodeVo> clients = new KeyValue<>();
        for (int i = 0; i < poolSize; i++) {
            RpcAutoReconnectClient client = new RpcAutoReconnectClient(nodeVo.getHost(), nodeVo.getPort(), nioEventLoopGroup);
            client.reConnect();
            // 链接本身, 注册node节点的启动时间, 本次链接注册node的开始时间
            list.add(client);
        }
        // 同时建立心跳链接
        RpcHeartClient rpcHeartClient = new RpcHeartClient(nodeVo.getHost(), nodeVo.getPort(), nioEventLoopGroup);
        rpcHeartClient.connect();

        clients.setData(nodeVo);
        clients.setValue(rpcHeartClient);
        clients.setKey(list);
        clientPool.put(nodeVo.getName(), clients);
    }

    /**
     * 销毁连接
     */
    public static void destory(String nodeName) {
        KeyValue<List<RpcAutoReconnectClient>, RpcHeartClient, NodeVo> remove = clientPool.remove(nodeName);
        if (remove == null) {
            return;
        }
        List<RpcAutoReconnectClient> key = remove.getKey();
        for (int i = 0; i < key.size(); i++) {
            RpcAutoReconnectClient client = key.get(i);
            ThreadUtil.execSilentVoid(() -> {
                client.closeChannel();
            });
        }
    }

    /**
     * 检测连接是否可用个,不可用将予以清除
     */
    public static void monitorAndDestory() {
        Iterator<Map.Entry<String, KeyValue<List<RpcAutoReconnectClient>, RpcHeartClient, NodeVo>>> iterator = clientPool.entrySet().iterator();
        while (iterator.hasNext()) {
            ThreadUtil.execSilentVoid(() -> {
                Map.Entry<String, KeyValue<List<RpcAutoReconnectClient>, RpcHeartClient, NodeVo>> next = iterator.next();
                KeyValue<List<RpcAutoReconnectClient>, RpcHeartClient, NodeVo> value = next.getValue();


                List<RpcAutoReconnectClient> clients = value.getKey();
                RpcHeartClient rpcHeartClient = value.getValue();
                if (!rpcHeartClient.isActive()) {
                    for (RpcAutoReconnectClient autoReconnectClient : clients) {
                        ThreadUtil.execSilentVoid(() -> {
                            autoReconnectClient.closeChannel();
                        });
                    }
                    NodeVo data = value.getData();
                    logger.info("清理链接:{}:{}", data.getHost(), data.getPort());
                    iterator.remove();
                }
            });
        }
    }

    /**
     * 根据名称获取连接
     *
     * @param nodeName
     * @return
     */
    public static RpcAutoReconnectClient get(String nodeName) {
        KeyValue<List<RpcAutoReconnectClient>, RpcHeartClient, NodeVo> kv = clientPool.get(nodeName);
        if (kv == null || CollectionUtils.isEmpty(kv.getKey())) {
            return null;
        }
        List<RpcAutoReconnectClient> key = kv.getKey();
        return key.get(new Random().nextInt(key.size()));
    }

    /**
     * 根据名称获取连接
     *
     * @param
     * @return
     */
    public static List<NodeVo> nodeList() {
        List<NodeVo> result = new ArrayList<>();
        clientPool.forEach((k, v) -> {
            NodeVo data = JsonUtil.parseObject(v.getData(), NodeVo.class);
            RpcHeartClient value = v.getValue();
            data.setActive(value.isActive());
            result.add(data);

        });
        return result;
    }

    /**
     * 获取连接
     *
     * @param nodeName
     * @return
     */
    public static RpcAutoReconnectClient getOrConnectClient(String nodeName) {
        RpcAutoReconnectClient rpcDefaultClient = ClientSitePool.get(nodeName);
        if (rpcDefaultClient != null) {
            return rpcDefaultClient;
        }
        RpcAutoReconnectClient centerClient = getCenterClient();
        if (centerClient == null) {
            throw new RuntimeException("未获取到中心节点有效连接:" + nodeName);
        }
        RpcRequest request = new RpcRequest();
        request.setRequestType(RequestTypeEnmu.getNode.name());
        request.setBody(nodeName);
        RpcFuture rpcFuture = centerClient.sendSynMsg(request);
        RpcResponse rpcResponse = null;
        try {
            rpcResponse = rpcFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (rpcResponse == null) {
            throw new RuntimeException("未查询到接目标:" + nodeName + " 节点信息");
        }
        String body = rpcResponse.getBody();
        NodeVo nodeVo = JsonUtil.parseObject(body, NodeVo.class);

        if (nodeVo == null) {
            throw new RuntimeException("未查询到接目标:" + nodeName + " 节点信息");
        }

        ClientSitePool.accept(nodeVo);
        return ClientSitePool.get(nodeVo.getName());
    }

    /**
     * 获取注册节点连接
     *
     * @return
     */
    public static RpcAutoReconnectClient getCenterClient() {
        List<NodeVo> nodeVos = EnvConfig.centerNodes();
        List<NodeVo> newList = new ArrayList<>(nodeVos);
        Collections.shuffle(newList);
        for (int i = 0; i < newList.size(); i++) {
            String centerNode = newList.get(0).getName();
            RpcAutoReconnectClient client = ClientSitePool.get(centerNode);
            if (client != null) {
                return client;
            }
        }
        return null;
    }
}