package com.txy.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.txy.im.common.config.AppConfig;
import com.txy.im.common.constant.Constants;
import com.txy.im.common.enums.ConversationTypeEnum;
import com.txy.im.common.enums.DelFlagEnum;
import com.txy.im.common.model.message.*;
import com.txy.im.service.conversation.service.ConversationService;
import com.txy.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.txy.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.txy.im.service.message.dao.ImMessageBodyEntity;
import com.txy.im.service.message.dao.ImMessageHistoryEntity;
import com.txy.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.txy.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.txy.im.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/8 15:47
 */
@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Autowired
    RedisTemplate stringRedisTemplate;

    @Autowired
    AppConfig appConfig;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ConversationService conversationService;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {
        // messageContent 转化为 messageBody

        // 插入messageBody
        // imMessageBodyMapper.insert(messageBody);
        // 转化成 messageHistory
        // List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(messageContent, messageBody);

        // 批量插入
        //  imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
        //  messageContent.setMessageKey(messageBody.getMessageKey());

        ImMessageBody messageBody = extractMessageBody(messageContent);
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        dto.setMessageBody(messageBody);
        messageContent.setMessageKey(messageBody.getMessageKey());
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreP2PMessage, "", JSONObject.toJSONString(dto));
        //TODO 发送消息*/-+-*

    }

    public ImMessageBody extractMessageBody(MessageContent messageContent) {
        ImMessageBody messageBody = new ImMessageBody();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(SnowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent, ImMessageBodyEntity imMessageBodyEntity) {

        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);

        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(messageContent.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();

        BeanUtils.copyProperties(messageContent, toHistory);

        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(messageContent.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;

    }

    //群聊采取的是读扩散
    @Transactional
    public void storeGroupMessage(GroupChatMessageContent messageContent) {
      /*  // messageContent 转化为 messageBody
        ImMessageBodyEntity messageBody = extractMessageBody(messageContent);

        //插入messageBody
        imMessageBodyMapper.insert(messageBody);
        //转化成 messageHistory
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, messageBody);

        //批量插入
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
        messageContent.setMessageKey(messageBody.getMessageKey());*/
    }


    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent
                                                                             messageContent, ImMessageBodyEntity messageBodyEntity) {
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }

    /***
     * 保证消息的幂等性
     * @param appId
     * @param messageId
     * @param messageContent
     */
    public void setMessageFromMessageIdCache(Integer appId, String messageId, Object messageContent) {
        //appid : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(messageContent), 300, TimeUnit.SECONDS);
    }

    public <T> T getMessageFromMessageIdCache(Integer appId,
                                              String messageId, Class<T> clazz) {
        //appid : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        String msg = (String) stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        return JSONObject.parseObject(msg, clazz);
    }

    /**
     * @param
     * @return void
     * @description: 存储单人离线消息
     * @author lld
     */
    public void storeOfflineMessage(OfflineMessageContent offlineMessage) {

        // 找到fromId的队列
        String fromKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getFromId();
        // 找到toId的队列
        String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getToId();

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断 队列中的数据是否超过设定值
        if (operations.zCard(fromKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(fromKey, 0, 0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(), offlineMessage.getFromId(), offlineMessage.getToId()
        ));
        // 插入 数据 根据messageKey 作为分值
        operations.add(fromKey, JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey());

        //判断 队列中的数据是否超过设定值
        if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(toKey, 0, 0);
        }

        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(), offlineMessage.getToId(), offlineMessage.getFromId()
        ));
        // 插入 数据 根据messageKey 作为分值
        operations.add(toKey, JSONObject.toJSONString(offlineMessage),
                offlineMessage.getMessageKey());

    }


    /**
     * @param
     * @return void
     * @description: 存储单人离线消息
     * @author lld
     */
    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage
            , List<String> memberIds) {

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断 队列中的数据是否超过设定值
        offlineMessage.setConversationType(ConversationTypeEnum.GROUP.getCode());

        for (String memberId : memberIds) {
            // 找到toId的队列
            String toKey = offlineMessage.getAppId() + ":" +
                    Constants.RedisConstants.OfflineMessage + ":" +
                    memberId;
            offlineMessage.setConversationId(conversationService.convertConversationId(
                    ConversationTypeEnum.GROUP.getCode(), memberId, offlineMessage.getToId()
            ));
            if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
                operations.removeRange(toKey, 0, 0);
            }
            // 插入 数据 根据messageKey 作为分值
            operations.add(toKey, JSONObject.toJSONString(offlineMessage),
                    offlineMessage.getMessageKey());
        }


    }
}
