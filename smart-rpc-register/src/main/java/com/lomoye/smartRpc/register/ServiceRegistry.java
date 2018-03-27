package com.lomoye.smartRpc.register;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lomoye on 2018/3/23.
 * 服务注册
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private String registryAddress;//服务注册中心地址

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String data) {
        //连接zookeeper
        ZkClient zooKeeper = connectZookeeper();

        if (zooKeeper == null) {
            throw new RuntimeException("register zooKeeper failed");
        }

        //向zookeeper创建数据节点
        createNode(zooKeeper, data);
    }

    private void createNode(ZkClient zkClient, String data) {
        // 创建 registry 节点（持久）
        String registryPath = Constant.ZK_REGISTRY_PATH;
        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
            LOGGER.debug("create registry node: {}", registryPath);
        }


        String addressPath = Constant.ZK_DATA_PATH;
        String addressNode = zkClient.createEphemeralSequential(addressPath, data);
        LOGGER.debug("create address node: {}", addressNode);
    }

    private ZkClient connectZookeeper() {
        // 创建 ZooKeeper 客户端
        ZkClient zkClient = new ZkClient(registryAddress, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT
                , new SerializableSerializer());
        LOGGER.debug("connect zookeeper");
        return zkClient;
    }


}
