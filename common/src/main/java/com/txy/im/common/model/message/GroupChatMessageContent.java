package com.txy.im.common.model.message;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: txy
 * @version: 1.0
 */
@Data
public class GroupChatMessageContent extends MessageContent {

    private String groupId;

    private List<String> memberId;

}
