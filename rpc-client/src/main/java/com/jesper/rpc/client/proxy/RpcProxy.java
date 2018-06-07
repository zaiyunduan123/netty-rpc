package com.jesper.rpc.client.proxy;

import com.jesper.rpc.client.discovery.ServiceDiscovery;
import com.jesper.rpc.client.netty.RpcClient;
import com.jesper.rpc.common.dto.RpcRequest;
import com.jesper.rpc.common.dto.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * 利用代理优化远程调用
 * 使之像本地调用一样
 * 动态代理对象类，用于根据接口创建动态代理对象
 */
public class RpcProxy {

    //用于发现服务的对象
    private ServiceDiscovery serviceDiscovery;

    private Logger logger = LoggerFactory.getLogger(RpcProxy.class);

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     *
     * @SuppressWarnings("unchecked")
     * 它是一个接口, 在java.lang包下.
       屏蔽警告信息(一般是函数中用到了过期的方法或是所给的参数类型不对).
       当你的编码可能存在警告时,比如安全警告,代码下就会出现一条黄色的波浪线,可以用它来消除.该批注的作用是给编译器一条指令,告诉它对被批注的代码元素内部的某些警告保持静默.
     */

    /**
     * 获得动态代理对象的通用方法，实现思路：该方法中，并不需要具体的实现类对象。因为在invoke方法中，并不会调用Method这个方法
     * 只是获取其方法的名字，然后将其封装在netty请求中，发送到metty服务端中请求远程调用的结果
     *
     * @param interfaceClass 需要被代理的接口的类型对象
     * @param <T>            对应接口的代理对象
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<?> interfaceClass) {
        T proxy = (T) Proxy.newProxyInstance(RpcProxy.class.getClassLoader(), new Class<?>[]{interfaceClass},
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        logger.info("准备构建RPCRequest对象...");
                        // 构建RpcRequest
                        RpcRequest request = new RpcRequest();
                        //设置requestId
                        request.setRequestId(UUID.randomUUID().toString());
                        // 设置接口
                        String interfaceName = method.getDeclaringClass().getName();
                        request.setInterfaceName(interfaceName);
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        // 设置参数列表parameters
                        request.setParameters(args);

                        logger.info("RPCRequest对象构建完毕，准备发现服务[{}]...", interfaceName);
                        // 发现服务，得到服务地址，格式为 host:port
                        String serverAddress = serviceDiscovery.discoverService(interfaceName);
                        //如果服务不存在，null,否则就构建rpc客户端进行远程调用
                        if (serverAddress == null) {
                            logger.error("服务[{}]的提供者不存在，发现服务失败...", interfaceName);
                            return null;
                        } else {
                            logger.info("发现服务完毕，准备解析服务地址[{}]...", serverAddress);
                            //解析服务地址
                            String[] array = serverAddress.split(":");
                            String host = array[0];
                            int port = Integer.valueOf(array[1]);

                            logger.info("服务地址解析完毕，准备构建RPC客户端...");
                            //构建rpc客户端
                            RpcClient client = new RpcClient(host, port);

                            logger.info("RPC客户端构建完毕，准备向RPC服务端发送请求...");

                            //向rpc服务端发送请求,返回信息
                            RpcResponse response = client.sendRequest(request);

                            if (response.isError()) {
                                throw response.getError();
                            } else {
                                //如果没有异常，则返回调用的结果
                                logger.info("[{}]远程过程调用完毕，远程过程调用成功...", interfaceName);
                                return response.getResult();
                            }
                        }
                    }
                });
        return proxy;
    }
}
