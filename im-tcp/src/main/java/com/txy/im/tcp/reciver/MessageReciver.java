package com.txy.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.txy.im.codec.proto.MessagePack;
import com.txy.im.common.constant.Constants;
import com.txy.im.tcp.reciver.process.BaseProcess;
import com.txy.im.tcp.reciver.process.ProcessFactory;
import com.txy.im.tcp.utils.MqFactory;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author tianxueyang
 * @version 1.0
 * @description 处理发送来的消息
 * @date 2023/9/3 17:04
 */
@Log4j
public class MessageReciver {

    private static String brokerId;

    private static void startReciverMessage() {
        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MessageService2Im + brokerId);
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im + brokerId, true, false, false, null);
            channel.queueBind(Constants.RabbitConstants.MessageService2Im + brokerId, Constants.RabbitConstants.MessageService2Im, brokerId);
            channel.basicConsume(Constants.RabbitConstants.MessageService2Im, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //处理mq发送来的消息


                    try{
                        String msgStr = new String(body);
                        MessagePack messagePack = JSONObject.parseObject(msgStr, MessagePack.class);
                        BaseProcess messageProcess = ProcessFactory.getMessageProcess(messagePack.getCommand());
                        messageProcess.process(messagePack);
                        log.info(msgStr);
                        channel.basicAck(envelope.getDeliveryTag(),false);
                        super.handleDelivery(consumerTag, envelope, properties, body);
                        log.info(msgStr);
                    }catch (Exception e){
                        channel.basicNack(envelope.getDeliveryTag(), false,false);
                        log.error(e);
                    }
                }
            });
        } catch (Exception e) {
            log.equals(e.getMessage());
        }
    }

    public static void init() {
        startReciverMessage();
    }

    public static void init(String brokerId) {
        if (StringUtils.isBlank(MessageReciver.brokerId)) {
            MessageReciver.brokerId = brokerId;
        }
        startReciverMessage();
    }
}
