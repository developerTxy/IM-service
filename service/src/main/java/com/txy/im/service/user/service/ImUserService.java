package com.txy.im.service.user.service;

import com.txy.im.common.ResponseVO;
import com.txy.im.service.user.dao.ImUserDataEntity;
import com.txy.im.service.user.model.req.*;
import com.txy.im.service.user.model.resp.GetUserInfoResp;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/29 16:44
 */
public interface ImUserService {


    public ResponseVO  importUser(ImportUserReq req);

    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId,Integer appId);

    public ResponseVO deleteUser(DeleteUserReq req);

    public ResponseVO modifyUserInfo(ModifyUserInfoReq req);

    public ResponseVO login(LoginReq req);

    ResponseVO getUserSequence(GetUserSequenceReq req);
}
