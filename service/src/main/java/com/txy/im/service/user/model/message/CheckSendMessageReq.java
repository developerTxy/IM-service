package com.txy.im.service.user.model.message;

import lombok.Data;
import sun.dc.pr.PRError;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class CheckSendMessageReq {

    private String fromId;

    private String toId;

    private Integer appId;

    private Integer command;

}
