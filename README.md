### Directory Structure

```
├── rpc-client                                // RPC客户端
│   ├── src/main
│   ├── ├──java/com/jesper/rpc/client         
│   ├── ├──├──discovery                       
│   ├── ├──├──├──ServiceDiscovery             // 服务发现类，用于向zookeeper中查询服务提供者的地址
│   ├── ├──├──netty                           
│   ├── ├──├──├──RpcClient                    // rpc客户端，用于连接rpc服务端，向服务端发送请求
│   ├── ├──├──proxy                   
│   ├── ├──├──├──RpcProxy                     // 动态代理对象类，用于根据接口创建动态代理对象
│   ├── ├──resources                           
│   ├── ├──├──log4j.properties                // log4j基本配置
├── rpc-common                                // 公共模块
│   ├── src/main
│   ├── ├──java/com/jesper/rpc/common             
│   ├── ├──├──annotation                     
│   ├── ├──├──├──ServiceExporter              // 自定义注解 服务提供方发布服务的注解       
│   ├── ├──├──codec                             
│   ├── ├──├──├──RpcDecoder                   // 解码器 : 负责解码网络上过来的数据
│   ├── ├──├──├──RpcEncoder                   // 编码器：负责将Object类型的POJO对象编码为byte数组
│   ├── ├──├──dto                            
│   ├── ├──├──├──RpcRequest                   // client向server端发送数据的传输载体
│   ├── ├──├──├──RpcResponse                  // server向client端发送数据的传输载体
│   ├── ├──├──util                          
│   ├── ├──├──├──SerializationUtil            // 序列化工具类
├── rpc-server                                // RPC服务端
│   ├── src/main
│   ├── ├──java/com/jesper/rpc/server           
│   ├── ├──├──netty                           
│   ├── ├──├──├──RpcServer                    // rpc服务端，用于向zookeeper注册需要发布的服务
│   ├── ├──├──├──RpcServerHandler             // rpc服务端处理类，负责处理用户的请求，并返回响应结果
│   ├── ├──├──registry                       
│   ├── ├──├──├──ServiceRegistry              // 服务注册类，用于将服务提供者的服务注册到zookeeper上
│   ├── ├──├──├──TestZk                       // zookeeper测试类
│   ├── ├──resources                          
│   ├── ├──├──log4j.properties                // log4j基本配置
├── .gitignore                                 
├── pom.xml                                   // parent pom               
├── README.md               
```