package com.jesper.rpc.client.discovery;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by jiangyunxiong on 2018/6/7.
 * <p>
 * 服务发现类，用于向zookeeper中查询服务提供者的地址（host:port）
 */
public class ServiceDiscovery {
    // zookeeper中保存服务信息的父节点
    private final String parentNode = "/rpc";
    // zookeeper的地址，由spring构造ServiceDiscovery对象时传入
    private String registryAddress;

    private int sessionTimeout = 2000;

    private ZooKeeper zkClient = null;
    // 用来确保zookeeper连接成功后才进行后续的操作
    private CountDownLatch latch = new CountDownLatch(1);
    // log4j日志记录
    private Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    /**
     * 发现服务方法 根据接口名称向zookeeper查询服务提供者的地址
     */
    public String discoverService(String interfaceName) {
        if (this.zkClient == null) {
            logger.info("未连接zookeeper，准备建立连接...");
            connectServer();
        }
        //构建需要查询的节点的完整名称
        String node = parentNode + "/" + interfaceName;
        //获取该节点所对应的服务提供者地址
        logger.info("zookeeper连接建立完毕，准备获取服务提供者地址[{}]...", node);
        String serverAddress = getServerAddress(node);
        logger.info("服务提供者地址获取完毕[{}]...", serverAddress);
        // 返回结果
        return serverAddress;

    }

    /**
     * 建立连接
     */
    private void connectServer() {
        try {
            zkClient = new ZooKeeper(registryAddress, sessionTimeout, new Watcher() {

                // 注册监听事件，连接成功后会调用process方法
                // 此时再调用latch的countDown方法使CountDownLatch计数器减1
                // 因为构造CountDownLatch对象时设置的值为1，减1后变为0，所以执行该方法后latch.await()将会中断
                // 从而确保连接成功后才会执行后续zookeeper的相关操作
                @Override
                public void process(WatchedEvent event) {
                    // 如果状态为已连接，则使用CountDownLatch计数器减1
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取对应接口名的服务地址
     */
    private String getServerAddress(String node) {
        String serverAddress = null;
        try {
            // 先获取接口名节点的子节点，子节点下是服务器的列表
            // 需要注意的是，如果不存在该节点，会有异常，此时下面的代码就不会执行
            List<String> children = zkClient.getChildren(node, false);

            // 负载均衡：一致性hash算法
            ConsistentHash.initServers(children);
            String firstChildren = ConsistentHash.getServer("children");
            // 构建该服务提供者的完整节点名称
            String firstChildrenNode = node + "/" + firstChildren;
            // 获取服务提供者节点的数据，得到serverAddress的byte数组
            byte[] serverAddressByte = zkClient.getData(firstChildrenNode, false, null);
            // 将byte数组转换为字符串，同时赋值给serverAddress
            serverAddress = new String(serverAddressByte);
        } catch (Exception e) {
            logger.error("节点[{}]不存在，无法获取服务提供者地址...", node);
            logger.error(e.getMessage());
        }
        return serverAddress;
    }



    public static void main(String[] args) throws Exception {
//        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("localhost:2181");
//        serviceDiscovery.connectServer();
//        String serverAddress = serviceDiscovery.getServerAddress("/rpc/com.jyxmust.UserService");
//        System.out.println("serverAddress: " + serverAddress);


        List<String> servers = new ArrayList<>();
        servers.add("192.168.0.0:111");
        servers.add("192.168.0.1:111");
        servers.add("192.168.0.3:111");
        ConsistentHash.initServers(servers);
        String firstChildren = ConsistentHash.getServer("166");
        System.out.println(firstChildren);
    }

}
