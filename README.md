# node-file-nets
## 节点启动:
1. com.murong.rpc.AppMain
2. 打包后以jar包方式启动,每个jar启动后都认为是一个节点,多节点行成一个集群
3. 集群需要再linux机器上,单机测试可以在win上

## 对外服务的controller
com.murong.rpc.controller.NodeController
1. 该服务主要对外提供一些节点的服务信息
2. 服务启动后,可以进行调试
3. 节点与节点之间的信息传递主要是靠 rpc
   com.murong.rpc.service.RpcMsgService
    
   由一个节点向另外一个节点文件传输时: 先查询rpc链接,不存在则创建rpc,继而传输文件
4. com.murong.rpc.config.PoolManagerRunner
   该类启动后执行:
        主要是同步各个节点之间的元数据信息,同时向主节点注册信息
主节点则管理整个集群的连接信息
        







