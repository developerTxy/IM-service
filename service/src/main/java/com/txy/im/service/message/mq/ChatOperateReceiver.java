package com.txy.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.txy.im.common.enums.command.MessageCommand;
import com.txy.im.common.model.message.MessageContent;
import com.txy.im.service.message.service.MessageSyncService;
import com.txy.im.service.message.service.P2pMessageService;
import com.txy.im.service.user.model.message.MessageReciveAckContent;
import org.springframework.amqp.core.Message;
import com.txy.im.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author tianxueyang
 * @version 1.0
 * @description im想service 投递消息
 * @date 2023/9/7 19:31
 */

@Component
public class ChatOperateReceiver {

    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);

    @Autowired
    private P2pMessageService p2pMessageService;
    @Autowired
    MessageSyncService messageSyncService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = Constants.RabbitConstants.Im2MessageService, durable = "true")
            , exchange = @Exchange(value = Constants.RabbitConstants.Im2MessageService, durable = "true")), concurrency = "1")
    public void onChatMessage(@Payload Message message, @Header Map<String, Object> headers, Channel channel) throws IOException {

        String msg = new String(message.getBody(), "utf-8");
        logger.info("ChAT MSG FROM QUEUE:::{}", msg);
        Long deliveryTag =(Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(msg);
            Integer command = jsonObject.getInteger("command");
            if (command.equals(MessageCommand.MSG_P2P.getCommand())) {
                //处理消息
                MessageContent messageContent = jsonObject.toJavaObject(MessageContent.class);
                p2pMessageService.process(messageContent);
            }else if (command.equals(MessageCommand.MSG_RECIVE_ACK.getCommand())){
                //消息接受确认
                MessageReciveAckContent messageReciveAckContent = jsonObject.toJavaObject(MessageReciveAckContent.class);
                messageSyncService.receiveMark(messageReciveAckContent);

            }
            channel.basicAck(deliveryTag,false);
        }catch (Exception e){
            logger.error("处理消息出现异常；{}",e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR；{}",e);
            logger.error("NACK_MSG；{}",msg);
            channel.basicNack(deliveryTag,false,false);
        }
    }
}
