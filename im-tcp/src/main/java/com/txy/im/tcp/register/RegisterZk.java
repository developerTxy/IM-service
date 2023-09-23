package com.txy.im.tcp.register;

import com.txy.im.codec.config.BootstrapConfig;
import com.txy.im.common.constant.Constants;
import jdk.nashorn.internal.ir.CallNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/3 19:18
 */
public class RegisterZk implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RegisterZk.class);
    private ZKit zKit;
    private String ip;

    private BootstrapConfig.TcpConfig tcpConfig;

    public RegisterZk(ZKit zKit, String ip, BootstrapConfig.TcpConfig tcpConfig) {
        this.ip = ip;
        this.tcpConfig = tcpConfig;
        this.zKit = zKit;
    }


    @Override
    public void run() {
        zKit.createRootNode();
        String tcpPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp + "/" + ip + ":" + this.tcpConfig.getTcpPort();
        zKit.createNode(tcpPath);
        logger.info("Registry zookeeper tcpPath syuccess , msg =[{}]",tcpPath);

        String webPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb + "/" + ip + ":" + this.tcpConfig.getWebSocketPort();
        zKit.createNode(webPath);
        logger.info("Registry zookeeperwebPath syuccess , msg =[{}]",webPath);
    }
}
