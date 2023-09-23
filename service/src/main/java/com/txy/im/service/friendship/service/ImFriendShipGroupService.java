package com.txy.im.service.friendship.service;

import com.txy.im.common.ResponseVO;
import com.txy.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.txy.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.txy.im.service.friendship.model.req.DeleteFriendShipGroupReq;

/**
 * @author: Chackylee
 * @description:
 **/
public interface ImFriendShipGroupService {

    public ResponseVO addGroup(AddFriendShipGroupReq req);

    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);

    public Long updateSeq(String fromId, String groupName, Integer appId);
}
