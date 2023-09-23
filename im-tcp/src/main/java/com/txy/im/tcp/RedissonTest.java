package com.txy.im.tcp;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.RedisClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/1 11:44
 */
public class RedissonTest {

    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(0);
        StringCodec stringCodec = new StringCodec();
        config.setCodec(stringCodec);
        RedissonClient redisClient = Redisson.create(config);
      /*  RBucket<Object> im = redisClient.getBucket("im");
        System.out.println(im.get());
        im.set("im");
        System.out.println(im.get());*/

      /*  RMap<String, String> imMap = redisClient.getMap("imMap");
        String  client = imMap.get("client");
        System.out.println(client);
        imMap.put("client","webclient");
        System.out.println(imMap.get("client"));*/

        RTopic topic = redisClient.getTopic("topic");
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String s) {
                System.out.println("收到消息"+ s);
            }
        });
        topic.publish("hello");

    }
}
