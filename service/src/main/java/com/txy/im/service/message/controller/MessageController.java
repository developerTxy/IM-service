package com.txy.im.service.message.controller;

import com.txy.im.common.ResponseVO;
import com.txy.im.common.model.message.CheckSendMessageReq;
import com.txy.im.service.group.model.req.SendGroupMessageReq;
import com.txy.im.service.group.service.GroupMessageService;
import com.txy.im.service.message.model.req.SendMessageReq;
import com.txy.im.service.message.service.P2pMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/8 17:44
 */
@RestController
@RequestMapping("v1/message")
public class MessageController {

    @Autowired
    P2pMessageService p2pMessageService;

    @Autowired
    GroupMessageService groupMessageService;


    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId)  {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2pMessageService.send(req));
    }


    @RequestMapping("/sendMessage")
    public ResponseVO sendMessage(@RequestBody @Validated SendGroupMessageReq req, Integer appId,String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return ResponseVO.successResponse(groupMessageService.send(req));
    }

    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req)  {

        return p2pMessageService.imServerPermissionCheck(req.getFromId(),req.getToId(), req.getAppId());
    }
}
