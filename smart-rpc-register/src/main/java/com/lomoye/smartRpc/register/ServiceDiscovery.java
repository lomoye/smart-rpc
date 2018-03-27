package com.lomoye.smartRpc.register;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by lomoye on 2018/3/24.
 * 服务发现
 */
public class ServiceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<>();

    private String registryAddress;

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;

        ZkClient zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }

    public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                LOGGER.debug("using only data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("using random data: {}", data);
            }
        }
        return data;
    }

    private void watchNode(ZkClient zk) {
        try {
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH);
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                String data = zk.readData(Constant.ZK_REGISTRY_PATH + "/" + node);
                dataList.add(data);
            }
            LOGGER.debug("node data: {}", dataList);
            this.dataList = dataList;
        } finally {
            zk.close();
        }
    }

    private ZkClient connectServer() {
        ZkClient zkClient = null;
        try {
            // 创建 ZooKeeper 客户端
            zkClient = new ZkClient(registryAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
            LOGGER.debug("connect zookeeper");
        } catch (Exception e) {
            LOGGER.error("connectServer error", e);
        }

        return zkClient;
    }

}
