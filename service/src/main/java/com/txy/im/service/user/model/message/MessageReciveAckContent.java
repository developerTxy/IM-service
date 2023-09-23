package com.txy.im.service.user.model.message;

import com.txy.im.common.model.ClientInfo;
import lombok.Data;
import sun.dc.pr.PRError;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class MessageReciveAckContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;


}
