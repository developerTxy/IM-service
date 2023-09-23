package com.txy.im.tcp.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/1 16:41
 */
public class RabbitUtils {

    private static ConnectionFactory connectionFactory = new ConnectionFactory();
    static {
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");
    }

    public static Connection getConnection (){
        Connection conn = null;
        try{
            conn = connectionFactory.newConnection();
            return conn;
        }catch (Exception e){
            throw  new RuntimeException(e);
        }
    }
}
