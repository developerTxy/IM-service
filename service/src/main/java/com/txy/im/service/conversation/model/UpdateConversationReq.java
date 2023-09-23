package com.txy.im.service.conversation.model;

import com.txy.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description: 会话请求修改封装类
 * @author: txy
 * @version: 1.0
 */
@Data
public class UpdateConversationReq extends RequestBase {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private String fromId;


}
