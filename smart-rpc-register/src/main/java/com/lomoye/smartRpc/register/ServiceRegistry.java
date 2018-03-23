package com.lomoye.smartRpc.register;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by lomoye on 2018/3/23.
 * 服务注册
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private String registryAddress;//服务注册中心地址

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String data) {
        //连接zookeeper
        ZooKeeper zooKeeper = connectZookeeper();

        if (zooKeeper == null) {
            throw new RuntimeException("register zooKeeper failed");
        }

        //向zookeeper创建数据节点
        createNode(zooKeeper, data);
    }

    private void createNode(ZooKeeper zooKeeper, String data) {
        try {
            String node = zooKeeper.create(Constant.ZK_DATA_PATH, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.warn("createNode success|node={}", node);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("createNode error", e);
        }
    }

    private ZooKeeper connectZookeeper() {
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, event -> {
                if (Watcher.Event.KeeperState.SyncConnected.equals(event.getState())) {
                    countDownLatch.countDown();
                }
            });

            countDownLatch.await();
        } catch (Exception e) {
            LOGGER.error("register error", e);
        }

        return zooKeeper;
    }


}
