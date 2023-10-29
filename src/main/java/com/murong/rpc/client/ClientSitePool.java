package com.murong.rpc.client;

import com.murong.rpc.config.EnvConfig;
import com.murong.rpc.util.JsonUtil;
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
    private static final Map<String, KeyValue<List<RpcAutoReconnectClient>, Long, NodeVo>> clientPool = new ConcurrentHashMap<>();

    /**
     * 接收配置
     */
    public static void accept(NodeVo nodeVo) {
        if (clientPool.containsKey(nodeVo.getName())) {
            return;
        }
        List<RpcAutoReconnectClient> list = new ArrayList<>();
        KeyValue<List<RpcAutoReconnectClient>, Long, NodeVo> clients = new KeyValue<>();
        for (int i = 0; i < poolSize; i++) {
            RpcAutoReconnectClient client = new RpcAutoReconnectClient(nodeVo.getHost(), nodeVo.getPort(), nioEventLoopGroup);
            client.reConnect();
            // 链接本身, 注册node节点的启动时间, 本次链接注册node的开始时间
            list.add(client);
        }
        clients.setData(nodeVo);
        clients.setValue(nodeVo.getStartTime());
        clients.setKey(list);
        clientPool.put(nodeVo.getName(), clients);
    }

    /**
     * 销毁连接
     */
    public static void destory(String nodeName) {
        KeyValue<List<RpcAutoReconnectClient>, Long, NodeVo> remove = clientPool.remove(nodeName);
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
        Iterator<Map.Entry<String, KeyValue<List<RpcAutoReconnectClient>, Long, NodeVo>>> iterator = clientPool.entrySet().iterator();
        while (iterator.hasNext()) {
            ThreadUtil.execSilentVoid(() -> {
                Map.Entry<String, KeyValue<List<RpcAutoReconnectClient>, Long, NodeVo>> next = iterator.next();
                KeyValue<List<RpcAutoReconnectClient>, Long, NodeVo> value = next.getValue();
                List<NodeVo> nodeVos = EnvConfig.centerNodes();
                List<String> centerNodes = nodeVos.stream().map(NodeVo::getName).collect(Collectors.toList());

                List<RpcAutoReconnectClient> clients = value.getKey();

                if (!centerNodes.contains(next.getKey())) { // center节点会一直保证连接
                    if (CollectionUtils.isEmpty(clients)) {
                        iterator.remove();
                    } else {
                        // 判断是否有未激活的连接
                        long unActived = clients.stream().filter(t -> t.getChannel() != null && !t.getChannel().isActive()).count();
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
        KeyValue<List<RpcAutoReconnectClient>, Long, NodeVo> kv = clientPool.get(nodeName);
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
            NodeVo data = v.getData();
            result.add(data);

        });
        return result;
    }
}