package com.txy.im.tcp.reciver.process;

import com.txy.im.codec.proto.Message;
import com.txy.im.codec.proto.MessagePack;
import com.txy.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author tianxueyang
 * @version 1.0
 * @description 处理 mq发过来的消息
 * @date 2023/9/7 16:56
 */
public abstract class BaseProcess {

    public void process(MessagePack messagePack) {

        NioSocketChannel nioSocketChannel = SessionSocketHolder.get(messagePack.getAppId(), messagePack.getUserId(), messagePack.getClientType(), messagePack.getImei());
        if (nioSocketChannel != null) {
            nioSocketChannel.writeAndFlush(messagePack);
        }
        processAfter();
    }

    public abstract void processAfter();
}
