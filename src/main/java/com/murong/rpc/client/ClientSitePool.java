package com.murong.rpc.client;

import com.murong.rpc.config.EnvConfig;
import com.murong.rpc.util.KeyValue;
import com.murong.rpc.util.ThreadUtil;
import com.murong.rpc.vo.NodeVo;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClientSitePool {


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
    private static final Map<String, List<KeyValue<RpcAutoReconnectClient, Long, Long>>> clientPool = new ConcurrentHashMap<>();

    /**
     * 接收配置
     *
     * @param nodeName
     * @param nodeHost
     * @param port
     */
    public static void accept(String nodeName, String nodeHost, int port) {
        if (clientPool.containsKey(nodeName)) {
            return;
        }
        List<KeyValue<RpcAutoReconnectClient, Long, Long>> clients = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            RpcAutoReconnectClient client = new RpcAutoReconnectClient(nodeHost, port, nioEventLoopGroup);
            client.reConnect();

            KeyValue<RpcAutoReconnectClient, Long, Long> kv = new KeyValue<>();
            kv.setKey(client);
            kv.setValue(System.currentTimeMillis());
            kv.setData(System.currentTimeMillis());
            clients.add(kv);
        }
        clientPool.put(nodeName, clients);
    }

    /**
     * 接收配置
     */
    public static void accept(NodeVo nodeVo) {
        if (clientPool.containsKey(nodeVo.getName())) {
            return;
        }
        List<KeyValue<RpcAutoReconnectClient, Long, Long>> clients = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            RpcAutoReconnectClient client = new RpcAutoReconnectClient(nodeVo.getHost(), nodeVo.getPort(), nioEventLoopGroup);
            client.reConnect();
            // 链接本身, 注册node节点的启动时间, 本次链接注册node的开始时间
            KeyValue<RpcAutoReconnectClient, Long, Long> kv = new KeyValue<>();
            kv.setKey(client);
            kv.setValue(nodeVo.getStartTime());
            kv.setData(System.currentTimeMillis());
            clients.add(kv);
        }
        clientPool.put(nodeVo.getName(), clients);
    }

    /**
     * 销毁连接
     */
    public static void destory(String nodeName) {
        List<KeyValue<RpcAutoReconnectClient, Long, Long>> keyValue = clientPool.remove(nodeName);
        if (CollectionUtils.isEmpty(keyValue)) {
            return;
        }
        for (int i = 0; i < keyValue.size(); i++) {
            RpcAutoReconnectClient client = keyValue.get(i).getKey();
            ThreadUtil.execSilentVoid(() -> {
                client.closeChannel();
            });
        }
    }

    /**
     * 检测连接是否可用个,不可用将予以清除
     */
    public static void monitorAndDestory() {
        Iterator<Map.Entry<String, List<KeyValue<RpcAutoReconnectClient, Long, Long>>>> iterator = clientPool.entrySet().iterator();
        while (iterator.hasNext()) {
            ThreadUtil.execSilentVoid(() -> {
                Map.Entry<String, List<KeyValue<RpcAutoReconnectClient, Long, Long>>> next = iterator.next();
                List<KeyValue<RpcAutoReconnectClient, Long, Long>> value = next.getValue();
                List<NodeVo> nodeVos = EnvConfig.centerNodes();
                List<String> centerNodes = nodeVos.stream().map(NodeVo::getName).collect(Collectors.toList());
                if (!centerNodes.contains(next.getKey())) { // center节点会一直保证连接
                    if (CollectionUtils.isEmpty(value)) {
                        iterator.remove();
                    } else {
                        // 判断是否有未激活的连接
                        long unActived = value.stream().map(m -> m.getKey()).filter(t -> t.getChannel() != null && !t.getChannel().isActive()).count();
                        if (unActived > 0) { // 如果有,则销毁该连接
                            destory(next.getKey());
                            System.out.println("destry");
                        }
                    }
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
        List<KeyValue<RpcAutoReconnectClient, Long, Long>> keyValues = clientPool.get(nodeName);
        if (CollectionUtils.isEmpty(keyValues)) {
            return null;
        }
        Optional<KeyValue<RpcAutoReconnectClient, Long, Long>> first = keyValues.stream().sorted(Comparator.comparingLong(KeyValue::getData)).findFirst();
        return first.get().getKey();
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
            if (!CollectionUtils.isEmpty(v)) {
                NodeVo nodeVo = new NodeVo();
                nodeVo.setName(k);
                nodeVo.setHost(v.get(0).getKey().host);
                nodeVo.setActive(v.get(0).getKey().getChannel().isActive());
                nodeVo.setPort(v.get(0).getKey().getPort());
                nodeVo.setStartTime(v.get(0).getValue());
                result.add(nodeVo);
            }
        });
        return result;
    }
}