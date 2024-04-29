# node-file-nets
## 节点启动:
1. com.murong.nets.AppMain
2. 打包后以jar包方式启动,每个jar启动后都认为是一个节点,多节点行成一个集群
3. 集群需要再linux机器上,单机测试可以在win上

## 对外服务的NodeController
1. 该服务主要对外提供一些节点的服务信息
2. 服务启动后,可以进行调试
3. 节点与节点之间的信息传递主要是靠 rpc
   com.murong.nets.service.RpcMsgService
    
   由一个节点向另外一个节点文件传输时: 先查询rpc链接,不存在则创建rpc,继而传输文件
4. com.murong.nets.config.PoolManagerRunner
   该类启动后执行:
        主要是同步各个节点之间的元数据信息,同时向主节点注册信息
主节点则管理整个集群的连接信息
## 对外服务的SystemController
1. 提供查询节点的内存使用情况
2. 提供查询节点的cpu使用情况
3. 提供查询节点的硬盘使用情况
4. 提供查询节点的进程情况

## 外部配置文件
    server.port=8080
    node.dir.list[0]=/Users/yaochuang/Desktop/test
    node.base.main-nodes[0]=127.0.0.1:8000
    node.base.local-node-port=8000
    node.base.local-node-host=127.0.0.1
        
```angular2html
配置说明:
    server.port: 
        web服务,NodeController 和 SystemController 对外提供的web服务
    node.dir.list:
        可以配置多个,整个集群所有主机的工作目录,在这些目录中,文件是可以被读写的
        未在该目录下的文件,不能够写(但可以被删请注意)
    node.base.local-node-port:
        本机的节点rpc端口,用于节点之间的信息交互
    node.base.local-node-host:
        本机节点的ip, 单机测试可以用127.0.0.1代替,但是集群千万不要用127.0.0.1,否则别的主机无法找到该节点
        
```







