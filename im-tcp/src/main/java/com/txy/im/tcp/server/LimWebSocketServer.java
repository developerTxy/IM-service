package com.txy.im.tcp.server;

import com.txy.im.codec.config.BootstrapConfig;
import com.txy.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/31 17:15
 */
public class LimWebSocketServer {
    private int port;
    private final static Logger logger = LoggerFactory.getLogger(LimWebSocketServer.class);

    BootstrapConfig.TcpConfig config;
    EventLoopGroup mainGroup;
    EventLoopGroup subGroup;
    ServerBootstrap serverBootstrap;
    public LimWebSocketServer(BootstrapConfig.TcpConfig config) {
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
                        //socket 基于http协议 ，所以要有http编解码器
                        pipeline.addLast("http-codec",new HttpServerCodec());
                        //对写大数据流的支持
                        pipeline.addLast("http-chunked",new ChunkedWriteHandler());
                        //几乎所有的netty编程中都会使用次handler
                        pipeline.addLast("aggregator",new HttpObjectAggregator(65535));

                        /**
                         * websocket 服务器处理的协议，用于指定给客户端连接访问的路由 : /ws
                         * 本handler会帮你处理一些繁重的复杂的事
                         * 会帮你处理握手动作： handshaking（close, ping, pong） ping + pong = 心跳
                         * 对于websocket来讲，都是以frames进行传输的，不同的数据类型对应的frames也不同
                         */
                        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
//                        pipeline.addLast(new WebSocketMessageDecoder());
//                        pipeline.addLast(new WebSocketMessageEncoder());
                        pipeline.addLast(new NettyServerHandler(config.getBrokerId(),config.getLogicUrl()));




                    }
                });
        serverBootstrap.bind(port);
    }

    public void start(){
        this.serverBootstrap.bind(this.config.getTcpPort());
    }
}
