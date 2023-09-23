package com.txy.im.common.exception;

import com.txy.im.common.enums.FriendShipErrorCode;
import com.txy.im.common.enums.UserErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/29 16:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationException extends  RuntimeException{

    private int code;

    private String error;

    public ApplicationException(UserErrorCode modifyUserError) {
        this.code = modifyUserError.getCode();
        this.error =modifyUserError.getError();
    }


    public ApplicationException(FriendShipErrorCode friendRequestIsNotExist) {
        this.code = friendRequestIsNotExist.getCode();
        this.error =friendRequestIsNotExist.getError();
    }

    public ApplicationException(ApplicationExceptionEnum friendRequestIsNotExist) {
        this.code = friendRequestIsNotExist.getCode();
        this.error =friendRequestIsNotExist.getError();
    }

}
