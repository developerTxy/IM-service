package com.txy.im.service.user.model.req;

import com.txy.im.common.model.RequestBase;
import com.txy.im.service.user.dao.ImUserDataEntity;
import lombok.Data;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.util.List;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/29 17:17
 */
@Data
public class ImportUserReq extends RequestBase {
     private Integer appId;
    private List<ImUserDataEntity> userData;
}
