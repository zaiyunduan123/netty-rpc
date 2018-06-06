package com.jesper.rpc.server.netty;

import com.jesper.rpc.common.dto.RpcRequest;
import com.jesper.rpc.common.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by jiangyunxiong on 2018/6/6.
 * <p>
 * 负责处理用户的请求，并返回响应结果
 */
public class RPCServerHandler extends ChannelInboundHandlerAdapter {

    Map<String, Object> serviceBeanMap = null;
    // log4j日志记录
    Logger logger = LoggerFactory.getLogger(RPCServerHandler.class);


    public RPCServerHandler(Map<String, Object> serviceBeanMap) {
        this.serviceBeanMap = serviceBeanMap;
    }

    /**
     * 接收消息，处理消息，返回结果
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("接收到来自RPC客户端的连接请求...");

        RpcRequest request = (RpcRequest) msg;
        RpcResponse rpcResponse = new RpcResponse();
        //设置requestId
        rpcResponse.setRequestId(request.getRequestId());
        try{
            logger.info("准备调用handle方法处理request请求对象...");
        }catch (Throwable e){

        }
    }

    /**
     * 对request进行处理，其实就是通过反射进行调用的过程
     * @param request
     * @return
     * @throws Throwable
     */

    public Object handleReuqest(RpcRequest request) throws Throwable {

        String interfaceName = request.getInterfaceName();

        // 根据接口名拿到其实现类对象
        Object serivceBean = serviceBeanMap.get(interfaceName);
        //拿到要调用的方法名、参数类型、参数值
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // 拿到接口类对象
        Class<?> clazz = Class.forName(interfaceName);

        // 拿到实现类对象的指定方法
        Method method = clazz.getMethod(methodName, parameterTypes);
        // 通过反射调用方法
        logger.info("准备通过反射调用方法[{}]...", interfaceName);
        Object result = method.invoke(serivceBean, parameters);
        logger.info("通过反射调用方法完毕...");
        //返回结果
        return result;
    }
}

