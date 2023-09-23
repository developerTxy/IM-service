package com.txy.im.tcp.server;

import lombok.Data;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/31 17:42
 */
@Data
public class TestYml {
    private String test;
    private String name;
    private int age;

    @Override
    public String toString() {
        return "TestYml{" +
                "test='" + test + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
