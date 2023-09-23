package com.txy.im.tcp.redis;

import com.txy.im.codec.config.BootstrapConfig;
import com.txy.im.tcp.reciver.UserLoginMessageListener;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/1 14:43
 */
public class RedisManager {

    private static RedissonClient redissonClient;
    private static  Integer loginModel;

    public static void init(BootstrapConfig config) {
        loginModel =config.getLim().getLoginModel();
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getLim().getRedis());
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel);
        userLoginMessageListener.listenerUserLogin();

    }
    public static  RedissonClient getRedissonClient(){
        return redissonClient;
    }
}
