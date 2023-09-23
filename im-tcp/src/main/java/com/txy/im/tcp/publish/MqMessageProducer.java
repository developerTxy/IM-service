package com.txy.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.txy.im.codec.proto.Message;
import com.txy.im.common.constant.Constants;
import com.txy.im.tcp.utils.MqFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author tianxueyang
 * @version 1.0
 * @description 发送消息的方法
 * @date 2023/9/3 16:41
 */
@Slf4j
public class MqMessageProducer {

    public static void sendMessage(Message message,Integer command) {
        Channel channel = null;
        String channelName = Constants.RabbitConstants.Im2MessageService;
        if (command.toString().startsWith("2")){
            channelName = Constants.RabbitConstants.Im2GroupService;
        }
        try {
            Channel channel1 = MqFactory.getChannel(channelName);
            String messageData = JSONObject.toJSONString(message);
            JSONObject o = (JSONObject) JSON.toJSON(message.getMessagePack());
            o.put("command",command);
            o.put("clientType",message.getMessageHeader().getClientType());
            o.put("imei",message.getMessageHeader().getImei());
            o.put("appId",message.getMessageHeader().getAppId());

            channel1.basicPublish(channelName, "", null, JSONObject.toJSONString(message).getBytes());
        } catch (Exception e) {
            log.error("发送消息异常：{}", e.getMessage());
        }
    }
}
