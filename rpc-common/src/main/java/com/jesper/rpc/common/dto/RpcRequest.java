package com.jesper.rpc.common.dto;

/**
 * Created by jiangyunxiong on 2018/6/1.
 *
 * client向server端发送数据的传输载体,将要传输的对象封装到RpcRequest对象中
 */
public class RpcRequest {

    private String requestId;
    //接口名称
    private String interfaceName;
    //调用的方法名称
    private String methodName;
    //方法的参数类型
    private Class<?>[] parameterTypes;
    //方法的参数值
    private Object[] parameters;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
