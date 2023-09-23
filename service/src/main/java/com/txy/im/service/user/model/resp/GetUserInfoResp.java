package com.txy.im.service.user.model.resp;

import com.txy.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/30 13:50
 */
@Data
public class GetUserInfoResp {
    private List<ImUserDataEntity> userDataItem;

    private List<String> failUser;
}
