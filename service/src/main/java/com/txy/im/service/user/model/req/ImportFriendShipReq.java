package com.txy.im.service.user.model.req;

import com.txy.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/30 14:39
 */
@Data
public class ImportFriendShipReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    public String fromId;

    public List friendItem;

    @Data
    public  static class  ImportFriendDto{
        private String toId;
        private String remark;

        private String addSource;

        private Integer status;

        private  Integer black;
    }
}
