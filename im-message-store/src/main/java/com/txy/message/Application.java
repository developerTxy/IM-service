package com.txy.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/11 15:08
 */

@SpringBootApplication
@MapperScan("com.txy.im.message.dao.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
