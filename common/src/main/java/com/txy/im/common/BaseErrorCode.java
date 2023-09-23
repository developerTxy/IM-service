package com.txy.im.common;

import com.txy.im.common.exception.ApplicationExceptionEnum;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/29 16:08
 */
public enum BaseErrorCode  implements ApplicationExceptionEnum {
    SUCCESS(200,"success"),
    SYSTEM_ERROR(90000,"服务器内部错误，请联系管理员"),
    PARAMETER_ERROR(90001,"参数校验错误"),
    ;

    private int code;
private String error;
    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getError() {
        return null;
    }
     BaseErrorCode(Integer code,String err){
        this.code =code;
        this.error=err;
    }
}
