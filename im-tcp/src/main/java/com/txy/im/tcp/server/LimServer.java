package com.txy.im.tcp.server;

import com.txy.im.codec.MessageDecoder;
import com.txy.im.codec.MessageEncoder;
import com.txy.im.codec.config.BootstrapConfig;
import com.txy.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/31 16:45
 */
public class LimServer {

    private final static Logger logger = LoggerFactory.getLogger(LimServer.class);

    BootstrapConfig.TcpConfig config;
    EventLoopGroup mainGroup;
    EventLoopGroup subGroup;
    ServerBootstrap serverBootstrap;

    public LimServer(BootstrapConfig.TcpConfig config) {
        this.config =config;
         mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
         serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(mainGroup, subGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240) //服务端可连接队列大小
                .option(ChannelOption.SO_REUSEADDR, true) //参数表示允许使用本地地址和端口
                .childOption(ChannelOption.TCP_NODELAY, true) //是否禁用Nagle算法  简单点说是否批量发送数据包  true 关闭 false 开启 开启的话可以减少一定的网络开销，但影响消息实时性
                .childOption(ChannelOption.SO_KEEPALIVE, true) // 保持开关 2h 没有数据服务端会发送心跳包
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageEncoder());
                        pipeline.addLast(new IdleStateHandler(0,0,10));
                        pipeline.addLast(new NettyServerHandler(config.getBrokerId(), config.getLogicUrl()));
                    }
                });
        logger.info("web start");
    }

    public void start(){
        this.serverBootstrap.bind(this.config.getTcpPort());
    }
}
