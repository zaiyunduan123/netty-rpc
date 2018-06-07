##  关于RPC
RPC是指远程过程调用，也就是说两台服务器A、B，一个应用部署在A服务器上，想要调用B服务器上应用提供的函数或方法，由于不在一个内存空间，不能直接调用，需要通过网络来表达调用的语义和传达调用的数据。

1. 服务消费方（client）调用以本地调用方式调用服务；
2. 通过动态代理为调用类创建代理对象，在代理对象执行方法时拦截获取调用的方法的所有接口、方法名、参数集合和参数类型
3. client stub接收到调用后负责将方法、参数等组装成对象序列化（编码）之后通过网络进行传输； 
4. client stub找到服务地址，并将编码后的消息发送到服务端； 
5. server stub收到消息后进行反序列化（解码）； 
6. server stub根据解码结果调用本地的服务； 
7. 本地服务执行并将结果返回给server stub； 
8. server stub将返回结果序列化（编码）成消息发送至消费方； 
9. client stub接收到消息，并进行解码； 
10. 服务消费方（client）得到最终结果，整个RPC调用就完成了。

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
