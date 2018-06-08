package com.jesper.rpc.client.discovery;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by jiangyunxiong on 2018/6/8.
 */
public class ConsistentHash {

    //key表示服务器的hash值，value表示服务器
    private static SortedMap<Integer, String> sortedMap = new TreeMap<Integer, String>();

    //将所有服务器加入sortedMap
    public static void initServers( List<String> servers){
        for (int i = 0; i < servers.size(); i++) {
            int hash = getHash(servers.get(i));
            System.out.println("[" + servers.get(i) + "]加入集合中, 其Hash值为" + hash);
            sortedMap.put(hash, servers.get(i));
        }
    }

    public  static String getServer(String key) {
        int hash = getHash(key);

        SortedMap<Integer, String> subMap = sortedMap.tailMap(hash);
        if (subMap.isEmpty()) {
            //如果没有比该hash值大的，则从第一个node开始
            Integer i = sortedMap.firstKey();
            //返回对应的服务器
            return sortedMap.get(i);
        } else {
            //第一个Key就是顺时针过去离node最近的那个结点
            Integer i = subMap.firstKey();
            //返回对应的服务器
            return subMap.get(i);
        }
    }

    //使用FNV1_32_HASH算法计算服务器的Hash值,这里不使用重写hashCode的方法，最终效果没区别
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

    public static void main(String[] args) {
        String[] keys = {"sddsds", "dsdd", "dfdf"};
        for(int i=0; i<keys.length; i++)
            System.out.println("[" + keys[i] + "]的hash值为" + getHash(keys[i])
                    + ", 被路由到结点[" + getServer(keys[i]) + "]");
    }

}
