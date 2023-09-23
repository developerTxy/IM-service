package com.txy.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.txy.im.codec.pack.LoginPack;
import com.txy.im.codec.pack.message.ChatMessageAck;
import com.txy.im.codec.proto.Message;
import com.txy.im.codec.proto.MessageHeader;
import com.txy.im.codec.proto.MessagePack;
import com.txy.im.common.ResponseVO;
import com.txy.im.common.constant.Constants;
import com.txy.im.common.enums.ImConnectStatusEnum;
import com.txy.im.common.enums.command.GroupEventCommand;
import com.txy.im.common.enums.command.MessageCommand;
import com.txy.im.common.enums.command.SystemCommand;
import com.txy.im.common.model.UserClientDto;
import com.txy.im.common.model.UserSession;
import com.txy.im.common.model.message.CheckSendMessageReq;
import com.txy.im.common.model.message.GroupChatMessageContent;
import com.txy.im.tcp.feign.FeignMessageService;
import com.txy.im.tcp.publish.MqMessageProducer;
import com.txy.im.tcp.redis.RedisManager;
import com.txy.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.InterfaceAddress;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/1 10:22
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private Integer brokerId;
    private FeignMessageService feignMessageService;

    private String logicUrl;

    public NettyServerHandler(Integer brokerId, String logicUrl) {
        this.logicUrl = logicUrl;
        this.brokerId = brokerId;
        feignMessageService = Feign.builder().encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 1000))
                .target(FeignMessageService.class, "");
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Integer command = msg.getMessageHeader().getCommand();
        //登录commod
        if (command == SystemCommand.LOGIN.getCommand()) {
            LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()), new TypeReference<LoginPack>() {
            }.getType());
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(loginPack.getUserId());
            //将 channel 存起来
            /** 登陸事件 **/
            String userId = loginPack.getUserId();
            /** 为channel设置用户id **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(userId);
            String clientImei = msg.getMessageHeader().getClientType() + ":" + msg.getMessageHeader().getImei();
            /** 为channel设置client和imel **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientImei)).set(clientImei);
            /** 为channel设置appId **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(msg.getMessageHeader().getAppId());
            /** 为channel设置ClientType **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType))
                    .set(msg.getMessageHeader().getClientType());
            /** 为channel设置Imei **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.Imei))
                    .set(msg.getMessageHeader().getImei());

            //redis map 来存储用户session
            MessageHeader messageHeader = msg.getMessageHeader();
            UserSession session = new UserSession();
            session.setAppId(messageHeader.getAppId());
            session.setClientType(messageHeader.getClientType());
            session.setUserId(loginPack.getUserId());
            session.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            session.setBrokerId(brokerId);
            try {
                InetAddress localhost = InetAddress.getLocalHost();
                session.setBrokerHost(localhost.getHostAddress());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            //TODO  存到redis
            RedissonClient redisClient = RedisManager.getRedissonClient();

            RMap<String, String> map = redisClient.getMap(messageHeader.getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(msg.getMessageHeader().getClientType() + messageHeader.getImei(), JSONObject.toJSONString(session));
            SessionSocketHolder.put(messageHeader.getAppId(), loginPack.getUserId(), messageHeader.getClientType(), messageHeader.getImei(), (NioSocketChannel) ctx.channel());

            //处理多设备登陆
            UserClientDto dto = new UserClientDto();
            dto.setImei(msg.getMessageHeader().getImei());
            dto.setUserId(loginPack.getUserId());
            dto.setClientType(msg.getMessageHeader().getClientType());
            dto.setAppId(msg.getMessageHeader().getAppId());

            RTopic topic = redisClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(dto));

        } else if (command == SystemCommand.LOGOUT.getCommand()) { //退出登录操作 ，删除


            /** 为channel设置appId **/
            Integer appId = (Integer) ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).get();
            /** 为channel设置ClientType **/
            Integer clientType = (Integer) ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType)).get();
            /** 为channel设置用户id **/
            String userId = (String) ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).get();
            String imei = (String) ctx.channel().attr(AttributeKey.valueOf(Constants.Imei)).get();
            SessionSocketHolder.remove(appId, userId, clientType, imei);
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<Object, Object> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
            map.remove(clientType);
            ctx.close();
        } else if (command == SystemCommand.PING.getCommand()) {
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        } else if (command == MessageCommand.MSG_P2P.getCommand() || command == GroupEventCommand.MSG_GROUP.getCommand()) {
            try {
                CheckSendMessageReq checkSendMessageReq = new CheckSendMessageReq();
                checkSendMessageReq.setAppId(msg.getMessageHeader().getAppId());
                checkSendMessageReq.setCommand(msg.getMessageHeader().getCommand());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
                String fromId = jsonObject.getString("fromId");
                String toId = "";
                if (command ==MessageCommand.MSG_P2P.getCommand()){
                    toId = jsonObject.getString("toId");
                }else {
                    toId = jsonObject.getString("groupId");
                }

                checkSendMessageReq.setFromId(toId);
                // TODO 1：调用检验消息的发送方接口
                //如果成功直接投递到 mq
                ResponseVO responseVO = feignMessageService.checkSendMessage(checkSendMessageReq);
                if (responseVO.isOk()) {
                    MqMessageProducer.sendMessage(msg, command);
                } else {
                   Integer ackCommand =0;
                    if (command ==MessageCommand.MSG_P2P.getCommand()){
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    }else {
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }
                    //失败返回ack
                    ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                    responseVO.setData(chatMessageAck);
                    MessagePack<ResponseVO> ack = new MessagePack();
                    ack.setData(responseVO);
                    ack.setCommand(MessageCommand.MSG_ACK.getCommand());
                    ctx.channel().writeAndFlush(ack);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            MqMessageProducer.sendMessage(msg, command);
        }
        System.out.println(msg.toString());
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
}