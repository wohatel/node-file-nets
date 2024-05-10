package com.murong.nets.client;

import com.murong.nets.config.EnvConfig;
import com.murong.nets.constant.RequestTypeEnmu;
import com.murong.nets.interaction.RpcFuture;
import com.murong.nets.interaction.RpcRequest;
import com.murong.nets.interaction.RpcResponse;
import com.murong.nets.util.JsonUtil;
import com.murong.nets.util.KeyValue;
import com.murong.nets.util.RpcException;
import com.murong.nets.util.SecureRandomUtil;
import com.murong.nets.util.ThreadUtil;
import com.murong.nets.vo.NodeVo;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yaochuang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientSitePool {

    static Logger logger = LoggerFactory.getLogger(ClientSitePool.class);

    /**
     * 配置公用的eventLoopGroup
     */

    private static final NioEventLoopGroup NIO_EVENT_LOOP_GROUP = new NioEventLoopGroup();
    /**
     * 连接配置三个
     */
    private final static int poolSize = 3;

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
        // 如果已经链接了节点池,就不再继续链接
        if (clientPool.containsKey(nodeVo.getName())) {
            if (EnvConfig.getLocalNodeName().equals(nodeVo.getName())) {// 说明是中心节点自己向自己注册
                clientPool.get(nodeVo.getName()).setData(nodeVo);
            }
            return;
        }
        List<RpcAutoReconnectClient> list = new ArrayList<>();
        KeyValue<List<RpcAutoReconnectClient>, RpcHeartClient, NodeVo> clients = new KeyValue<>();
        for (int i = 0; i < poolSize; i++) {
            RpcAutoReconnectClient client = new RpcAutoReconnectClient(nodeVo.getHost(), nodeVo.getPort(), NIO_EVENT_LOOP_GROUP);
            client.reConnect();
            // 链接本身, 注册node节点的启动时间, 本次链接注册node的开始时间
            list.add(client);
        }
        // 同时建立心跳链接
        RpcHeartClient rpcHeartClient = new RpcHeartClient(nodeVo.getHost(), nodeVo.getPort(), NIO_EVENT_LOOP_GROUP);
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
        for (RpcAutoReconnectClient client : key) {
            ThreadUtil.execSilentVoid(client::closeChannel);
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
                        ThreadUtil.execSilentVoid(autoReconnectClient::closeChannel);
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
     */
    public static RpcAutoReconnectClient get(String nodeName) {
        KeyValue<List<RpcAutoReconnectClient>, RpcHeartClient, NodeVo> kv = clientPool.get(nodeName);
        if (kv == null || CollectionUtils.isEmpty(kv.getKey())) {
            return null;
        }
        List<RpcAutoReconnectClient> key = kv.getKey();
        return SecureRandomUtil.randomOf(key);
    }

    /**
     * 根据名称获取连接
     */
    public static List<NodeVo> nodeList() {
        List<NodeVo> result = new ArrayList<>();
        clientPool.forEach((k, v) -> {
            NodeVo data = v.getData();
            RpcHeartClient value = v.getValue();
            data.setActive(value.isActive());
            result.add(data);

        });
        return result;
    }

    /**
     * 获取连接
     */
    public static RpcAutoReconnectClient getOrConnectClient(String nodeName) {
        // 优先在自己连接池里面找
        RpcAutoReconnectClient rpcDefaultClient = ClientSitePool.get(nodeName);
        if (rpcDefaultClient != null) {
            return rpcDefaultClient;
        }
        RpcAutoReconnectClient centerClient = getCenterClient();
        RpcRequest request = new RpcRequest();
        request.setRequestType(RequestTypeEnmu.getNode.name());
        request.setBody(nodeName);
        RpcFuture rpcFuture = centerClient.sendSynMsg(request);
        RpcResponse rpcResponse = rpcFuture.get();
        if (rpcResponse == null) {
            throw new RpcException("未查询到接目标:" + nodeName + " 节点信息");
        }
        String body = rpcResponse.getBody();
        NodeVo nodeVo = JsonUtil.parseObject(body, NodeVo.class);

        if (nodeVo == null) {
            throw new RpcException("未查询到接目标:" + nodeName + " 节点信息");
        }

        ClientSitePool.accept(nodeVo);
        RpcAutoReconnectClient rpcAutoReconnectClient = ClientSitePool.get(nodeVo.getName());
        if (rpcAutoReconnectClient == null) {
            throw new RpcException("未链接到目标节点:" + nodeName);
        }
        return rpcAutoReconnectClient;
    }

    /**
     * 获取注册节点连接
     */
    public static RpcAutoReconnectClient getCenterClient() {
        List<NodeVo> nodeVos = EnvConfig.getCenterNodes();
        List<NodeVo> newList = new ArrayList<>(nodeVos);
        Collections.shuffle(newList);
        for (int i = 0; i < newList.size(); i++) {
            String centerNode = newList.get(0).getName();
            RpcAutoReconnectClient client = ClientSitePool.get(centerNode);
            if (client != null) {
                return client;
            }
        }
        throw new RpcException("getCenterClient: 未获取到中心链接");
    }

    /**
     * 关闭连接
     *
     * @param nodeName 指向节点的连接
     * @return boolean
     */
    public static boolean closeConnect(String nodeName) {
        KeyValue<List<RpcAutoReconnectClient>, RpcHeartClient, NodeVo> remove = clientPool.remove(nodeName);
        if (remove == null) {
            return false;
        }
        List<RpcAutoReconnectClient> clients = remove.getKey();
        for (RpcAutoReconnectClient client : clients) {
            ThreadUtil.execSilentVoid(client::closeChannel);
        }
        RpcHeartClient value = remove.getValue();
        ThreadUtil.execSilentVoid(value::closeChannel);
        return true;
    }

    /**
     * 清理所有链接
     *
     * @return boolean
     */
    public static boolean closeAllConnect() {
        Set<String> nodeNames = clientPool.keySet();
        for (String nodeName : nodeNames) {
            closeConnect(nodeName);
        }
        return true;
    }
}