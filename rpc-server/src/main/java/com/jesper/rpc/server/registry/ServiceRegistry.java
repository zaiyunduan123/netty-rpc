package com.jesper.rpc.server.registry;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 服务注册类，用于将服务提供者的服务注册到zookeeper上
 */
public class ServiceRegistry {

    //zookeeper中保存服务消息的父节点
    private final String parentNode = "/rpc";
    // zookeeper中服务提供者的序列化名称
    private final String serverName = "server";
    //zookeeper的地址，由spring构造ServiceRegistry对象时传入
    private String registryAddress;
    // 连接zookeeper的超时时间
    private int sessionTimeout = 2000;
    //连接zookeeper的客户端
    private ZooKeeper zkClient = null;
    // 用来确保zookeeper连接成功后才进行后续的操作
    private CountDownLatch latch = new CountDownLatch(1);
    // log4j日志记录
    private Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    /**
     * 向zookeeper注册服务
     */
    public void registerService(String serverAddress, String interfaceName) {

        if (this.zkClient == null) {
            logger.info("未连接zookeeper，准备建立连接...");
            connectServer();
        }
        logger.info("zookeeper连接建立成功，准备在zookeeper上创建相关节点...");
        //先判断父节点是否存在，如果不存在，则创建父节点
        if (!isExist(parentNode)) {
            logger.info("正在创建节点[{}]", parentNode);
            createPNode(parentNode, "");
        }
        //先判断接口节点 是否存在（即/rpc/interfacename），如果不存在，则先创建接口节点
        if (!isExist(parentNode + "/" + interfaceName)) {
            logger.info("正在创建节点[{}]", parentNode + "/" + interfaceName);
            createPNode(parentNode + "/" + interfaceName, "");
        }
        // 创建接口节点下的服务提供者节点（即/rpc/interfacename/provider00001）
        logger.info("正在创建节点[{}]", parentNode + "/" + interfaceName + "/" + serverName + "+序列号");
        createESNode(parentNode + "/" + interfaceName + "/" + serverName, serverAddress);
        logger.info("zookeeper上相关节点已经创建成功...");
    }

    /**
     * 创建永久节点（父节点/rpc和其子节点即接口节点需要创建为此种类型）
     *
     * @param node 节点的名称，父节点为/rpc，接口接点则为/rpc/interfacename
     * @param data 节点的数据，可为空
     */
    private void createPNode(String node, String data) {
        try {
            zkClient.create(node, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建短暂序列化节点
     *
     * @param node 节点的名称，如/rpc/interfacename/server00001
     * @param data 节点的数据，为服务提供者的IP地址和端口号的格式化数据，如192.168.100.101:21881
     */
    private void createESNode(String node, String data) {
        try {
            zkClient.create(node, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 建立连接
     */
    private boolean isExist(String node) {
        Stat stat = null;
        try {
            stat = zkClient.exists(node, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stat == null ? false : true;

    }

    private void connectServer() {
        try {
            zkClient = new ZooKeeper(registryAddress, sessionTimeout, new Watcher() {
                // 注册监听事件，连接成功后会调用process方法
                @Override
                public void process(WatchedEvent watchedEvent) {
                    // 如果状态为已连接，则使用CountDownLatch计数器减1
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                        logger.info("连接成功了。。。");
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServiceRegistry serviceRegistry = new ServiceRegistry("localhost:2181");
        serviceRegistry.connectServer();
        serviceRegistry.registerService("localhost:21881", "com.jyxmust.UserService");
        System.in.read();
        Map<String, String> map = new HashMap<>();
        map.put("name", "jyxmust");
        System.out.println(map.entrySet());
    }
}
