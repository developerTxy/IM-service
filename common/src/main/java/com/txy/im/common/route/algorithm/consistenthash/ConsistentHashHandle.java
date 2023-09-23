package com.txy.im.common.route.algorithm.consistenthash;

import com.txy.im.common.enums.UserErrorCode;
import com.txy.im.common.exception.ApplicationException;
import com.txy.im.common.route.RouteHandle;

import java.util.List;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/6 15:44
 */
public class ConsistentHashHandle  implements RouteHandle {
    private AbstractConsistentHash hash;

    public void setHash(AbstractConsistentHash hash){
        this.hash =hash;
    }
    @Override
    public String routeServer(List<String> values, String key) {
        //TreeMap 实现一致性哈希
        int size = values.size();
        if(size == 0){
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        return  hash.process(values,key);
    }
}
