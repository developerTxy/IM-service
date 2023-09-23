package com.txy.im.tcp;

import com.txy.im.codec.config.BootstrapConfig;
import com.txy.im.tcp.reciver.MessageReciver;
import com.txy.im.tcp.redis.RedisManager;
import com.txy.im.tcp.register.RegisterZk;
import com.txy.im.tcp.register.ZKit;
import com.txy.im.tcp.server.LimServer;
import com.txy.im.tcp.server.LimWebSocketServer;
import com.txy.im.tcp.server.TestYml;
import com.txy.im.tcp.utils.MqFactory;
import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import javax.imageio.stream.FileImageInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/31 16:44
 */
public class Starter {

    //Http get post put  delete

    // client ios anzhuo  windows  mac //支持 json  也支持 protobuf
    //imei  (指令  版本  clientType  消息解析类型 imei长度  appId  bodylen) + imei号 +

    //请求头
    public static void main(String[] args)  throws FileNotFoundException {
//        new LimServer(9000);
//        new LimWebSocketServer(19000);

        if (args.length>0) {
            start(args[0]);
        }

    }

    private  static  void start(String path){
        try {
            Yaml yaml = new Yaml();
            //path="D:\\WorkSpace\\code\\im_system\\im-tcp\\src\\main\\resources\\config.yml"
            InputStream fileInputStream = new FileInputStream(path);
            BootstrapConfig config = yaml.loadAs(fileInputStream, BootstrapConfig.class);
            System.out.println(config);
            new LimServer(config.getLim()).start();
            new LimWebSocketServer(config.getLim()).start();

            RedisManager.init(config);
            MqFactory.init(config.getLim().getRabbitmq());
            MessageReciver.init(config.getLim().getBrokerId().toString());

            registerZK(config);
        }catch (Exception e){
           e.printStackTrace();
            System.exit(500);
        }


    }
    public static void registerZK(BootstrapConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        ZkClient zkClient = new ZkClient(config.getLim().getZkConfig().getZkAddr(), config.getLim().getZkConfig().getZkConnectTimeOut());
        ZKit zKit = new ZKit(zkClient);
        RegisterZk registerZk = new RegisterZk(zKit, hostAddress, config.getLim());
        Thread thread =new Thread(registerZk);
        thread.start();;
    }
}
