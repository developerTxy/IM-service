package com.txy.im.service.seq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/11 16:56
 */
@Service
public class RedisSeq {

    @Autowired
    StringRedisTemplate redisTemplate;

    public Long doGetSeq(String key){
        return  redisTemplate.opsForValue().increment(key);
    }
}
