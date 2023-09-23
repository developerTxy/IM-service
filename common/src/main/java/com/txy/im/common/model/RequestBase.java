package com.txy.im.common.model;

import lombok.Data;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/29 17:29
 */
@Data
public class RequestBase {
    private  Integer appId;

    private String operater;

    private Integer clientType;

    private String imei;
}
