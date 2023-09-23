package com.txy.im.service.message.service;

import com.txy.im.codec.pack.message.ChatMessageAck;
import com.txy.im.codec.pack.message.MessageReciveServerAckPack;
import com.txy.im.common.ResponseVO;
import com.txy.im.common.constant.Constants;
import com.txy.im.common.enums.command.MessageCommand;
import com.txy.im.common.model.ClientInfo;
import com.txy.im.common.model.message.MessageContent;
import com.txy.im.service.message.model.req.SendMessageReq;
import com.txy.im.service.message.model.resp.SendMessageResp;
import com.txy.im.service.seq.RedisSeq;
import com.txy.im.service.utils.ConversationIdGenerate;
import com.txy.im.service.utils.MessageProducer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/7 20:15
 */
@Service
public class P2pMessageService {

    private Logger logger = LoggerFactory.getLogger(P2pMessageService.class);

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redSeq;


    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread();
                thread.setDaemon(true);
                thread.setName("message-process-thread-" + atomicInteger.getAndDecrement());
                return thread;
            }
        });
    }

    public void process(MessageContent messageContent) {
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        //TODO 用 messageId 用缓存中 消息id 保证消息处理的幂等性
        // ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
        //    if (responseVO.isOk()) {
        MessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(appId, messageContent.getMessageId(), MessageContent.class);
        if (messageFromMessageIdCache != null) {
            threadPoolExecutor.execute(() -> {
                //1.回   ack给自己
                ack(messageContent, ResponseVO.successResponse());
                //2.回   发消息给同步在线端
                syncToSender(messageContent, messageContent);
                //3.回   发消息给对方在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageContent);
                if (clientInfos.isEmpty()) {
                    //发送接收确认给发送方带上服务端表示
                    reciverAck(messageContent);
                }
            });

            return;
        }

        Long seq = redSeq.doGetSeq(messageContent.getAppId() +":"+ Constants.SeqConstants.Message +":"+  ConversationIdGenerate.generateP2PId(fromId, toId));
        messageContent.setMessageSequence(seq);
        messageStoreService.storeP2PMessage(messageContent);
        threadPoolExecutor.execute(() -> {
            //appId + Seq+(from + to )  group

            //1.回   ack给自己
            ack(messageContent, ResponseVO.successResponse());
            //2.回   发消息给同步在线端
            syncToSender(messageContent, messageContent);
            //3.回   发消息给对方在线端
            List<ClientInfo> clientInfos = dispatchMessage(messageContent);
            //将 messageId 存到缓存中
            messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(), messageContent);
            if (clientInfos.isEmpty()) {
                //发送接收确认给发送方带上服务端表示
                reciverAck(messageContent);
            }
        });


        //  } else {
        //好书自己 失败了
        //    ack(messageContent,responseVO);
//------        }
        //迁至校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是不是好友
        //回 ack
        //多段同步问题

    }

    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        logger.info("msg ack ,messageId ={},checkResult{}", messageContent.getMessageId(), responseVO);
        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        //发消息
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK, responseVO, messageContent);
    }

    public void reciverAck(MessageContent messageContent) {
        MessageReciveServerAckPack pack = new MessageReciveServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getFromId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setMessageSequence(messageContent.getMessageSequence());
        pack.setServerSend(true);
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_RECIVE_ACK,
                pack, new ClientInfo(messageContent.getAppId(), messageContent.getClientType()
                        , messageContent.getImei()));
    }

    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P, messageContent, messageContent);
    }

    private List<ClientInfo> dispatchMessage(MessageContent messageContent) {
        List<ClientInfo> clientInfos = messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P,
                messageContent, messageContent.getAppId());
        return clientInfos;
    }

    /**
     * 校验信息能不能发送的方法
     *
     * @param fromId
     * @param toId
     * @param appId
     * @return
     */
    public ResponseVO imServerPermissionCheck(String fromId, String toId, Integer appId) {
        ResponseVO responseVO = checkSendMessageService.checkSenderForvidAndMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }
        ResponseVO responseVO1 = checkSendMessageService.checkFriendShip(fromId, toId, appId);

        return responseVO1;
    }

    public SendMessageResp send(SendMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        MessageContent message = new MessageContent();
        BeanUtils.copyProperties(req, message);
        //插入数据
        messageStoreService.storeP2PMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        //2.发消息给同步在线端
        syncToSender(message, message);
        //3.发消息给对方在线端
        dispatchMessage(message);
        return sendMessageResp;
    }
}
