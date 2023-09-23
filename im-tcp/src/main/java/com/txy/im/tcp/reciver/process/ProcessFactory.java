package com.txy.im.tcp.reciver.process;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/9/7 16:55
 */
public class ProcessFactory {

    private static BaseProcess defaultProcess;

    static {
        defaultProcess = new BaseProcess() {
            @Override
            public void processAfter() {

            }
        };
    }

    public static BaseProcess getMessageProcess(Integer command) {
        return defaultProcess;
    }
}
