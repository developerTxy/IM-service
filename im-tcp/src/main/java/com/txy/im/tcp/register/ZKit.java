package com.txy.im.tcp.register;

import com.txy.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/3 19:09
 */
public class ZKit {

    ZkClient zkClient;
    public ZKit(ZkClient zkClient){
        this.zkClient =zkClient;
    }

    //im-createRoot/tcp/ip:port
    public void createRootNode(){
        boolean exists =zkClient.exists(Constants.ImCoreZkRoot);
        if (!exists){
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }

        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        if (!tcpExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        }

        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        if (!webExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        }
    }

    //ip+port
    public void createNode(String path){
        if (!zkClient.exists(path)){
            zkClient.createPersistent(path);
        }
    }
}
