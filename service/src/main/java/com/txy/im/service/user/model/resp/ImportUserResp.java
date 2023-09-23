package com.txy.im.service.user.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/30 9:48
 */
@Data
public class ImportUserResp {

    private List<String> successId;
    private List<String> errorId;
}
