package com.jesper.rpc.server.netty;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.util.HashMap;
import java.util.Map;

/**
 * RPCServer主要完成下面的几个功能
 * 1、将需要发布的服务保存到一个map中
 * 2、启动netty服务端程序
 * 3、向zookeeper注册需要发布的服务
 */
public class RPCServer implements ApplicationContextAware, InitializingBean {

    //用来保存用户服务实现类对象，key为实现类的接口名称，value为实现类对象
    private Map<String, Object> serviceBeanMap = new HashMap<>();


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}
