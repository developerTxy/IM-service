package com.txy.im.tcp.feign;

import com.txy.im.common.ResponseVO;
import feign.Headers;
import feign.RequestLine;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/10 15:06
 */

public interface FeignMessageService {
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @RequestLine("Post /message/checkSend")
    public ResponseVO checkSendMessage(Object o);


}
