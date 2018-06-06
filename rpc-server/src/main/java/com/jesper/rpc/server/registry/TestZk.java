package com.jesper.rpc.server.registry;

import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * Created by jiangyunxiong on 2018/6/6.
 */
public class TestZk {
    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        // 连接zookeeper的超时时间
         int sessionTimeout = 2000;
        // 创建一个与服务器的连接
        ZooKeeper zk = new ZooKeeper("localhost:2181", sessionTimeout, new Watcher() {
            // 监控所有被触发的事件
            public void process(WatchedEvent event) {
                System.out.println("已经触发了" + event.getType() + "事件！");
            }
        });
        // 创建一个目录节点 ==>已经触发了 None 事件！
        zk.create("/testRootPath", "testRootData".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        // 创建一个子目录节点
        zk.create("/testRootPath/testChildPathOne", "testChildDataOne".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        // testRootData 此处false 不触发事件
        System.out.println(new String(zk.getData("/testRootPath", false, null)));

        // 取出子目录节点列表 ==>[testChildPathOne] 在节点/testRootPath的getChildren上设置观察
        System.out.println(zk.getChildren("/testRootPath", true));

        // 修改子目录节点数据 由于上面的修改数据不触发观察 这边不执行事件
        zk.setData("/testRootPath/testChildPathOne", "modifyChildDataOne".getBytes(), -1);

        // 目录节点状态：[5,5,1281804532336,1281804532336,0,1,0,0,12,1,6]
        System.out.println("目录节点状态：[" + zk.exists("/testRootPath", true) + "]");

        // 创建另外一个子目录节点 ==>已经触发了 NodeChildrenChanged 事件！
        zk.create("/testRootPath/testChildPathTwo", "testChildDataTwo".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        // testChildDataTwo
        System.out.println(new String(zk.getData("/testRootPath/testChildPathTwo", true, null)));

        // 删除子目录节点 已经触发了 NodeDeleted 事件！

        zk.delete("/testRootPath/testChildPathTwo", -1);
        zk.delete("/testRootPath/testChildPathOne", -1);
        // 删除父目录节点 已经触发了 NodeDeleted 事件！
        zk.delete("/testRootPath", -1);
        // 关闭连接
        zk.close();
    }
}
