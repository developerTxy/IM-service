package com.txy.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.txy.im.codec.proto.Message;
import com.txy.im.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/1 9:27
 */
public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {


        //Http get post put  delete

        // client ios anzhuo  windows  mac //支持 json  也支持 protobuf
        //imei  (指令  版本  clientType  消息解析类型 imei长度  appId  bodylen) + imei号 +

        //请求头(指令
        // 版本
        //clientType
        //消息解析类型
        //imei长度
        //appId
        //bodylen） + imei号 + 请求体
        if (in.readableBytes() <28){
            return;
        }
        /**
         * 获取commond
         */
        int command =in.readInt();
        /**
         * version
         */
        int version = in.readInt();
        /**
         * clientType
         */
        int clientType = in.readInt();

        /**
         * messageType
         */
        int messageType = in.readInt();

        /**
         * appId
         */
        int appId = in.readInt();

        /**
         * imeiLength 消息类型
         */
        int imeiLength = in.readInt();

        /**
         * bodyLength 消息类型
         */
        int bodyLength = in.readInt();

        if (in.readableBytes() <bodyLength+imeiLength){
            //重置读索引
            in.resetReaderIndex();
            return;
        }



        byte[] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData);

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setMessageType(messageType);
        messageHeader.setAppId(appId);
        messageHeader.setClientType(clientType);
        messageHeader.setCommand(command);
        messageHeader.setImei(imei);
        messageHeader.setLength(bodyLength);
        messageHeader.setImeiLength(imeiLength);
        messageHeader.setVersion(version);

        Message message =new Message();
        message.setMessageHeader(messageHeader);

        if (messageType ==0x0){
            String body = new String(imeiData);
            JSONObject jsonObject =(JSONObject) JSONObject.parse(body);
            message.setMessagePack(jsonObject);
        }
        in.markReaderIndex();
        out.add(message);
    }
}
