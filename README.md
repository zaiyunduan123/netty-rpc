##  RPC介绍
RPC，即 Remote Procedure Call（远程过程调用），说得通俗一点就是：两台服务器A、B，一个应用部署在A服务器上，想要调用B服务器上应用提供的函数或方法，由于不在一个内存空间，不能直接调用，需要通过网络来表达调用的语义和传达调用的数据。

## 技术选型
1. Netty：为了支持高并发，需要异步的IO，netty屏蔽了Java底层的NIO细节，因此用较少的代码即可构建出支撑网络通信的功能。
2. Protostuff：它基于Protobuf序列化框架，面向POJO，无需编写.proto 文件，Protostuff相比Java自带的序列化，在高并发情况下更具有优势。
3. ZooKeeper：提供服务注册与发现功能，开发分布式系统的必备选择，同时它也具备集群能力。

## 整体框架
![](https://img-blog.csdn.net/2018060922042183)

## 详细步骤
1. 服务消费方（client）调用以本地调用方式调用服务；
2. client stub接收到调用后负责将方法、参数等组装成能够进行网络传输的消息体；
3. client stub找到服务地址，并将消息发送到服务端；
4. server stub收到消息后进行解码；
5. server stub根据解码结果调用本地的服务；
6. 本地服务执行并将结果返回给server stub；
7. server stub将返回结果打包成消息并发送至消费方；
8. client stub接收到消息，并进行解码；
9. 服务消费方得到最终结果。

RPC的目标就是要2~8这些步骤都封装起来，让用户对这些细节透明。
## Directory Structure

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
