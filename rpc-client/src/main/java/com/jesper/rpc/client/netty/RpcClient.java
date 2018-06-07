package com.jesper.rpc.client.netty;

import com.jesper.rpc.common.codec.RpcDecoder;
import com.jesper.rpc.common.codec.RpcEncoder;
import com.jesper.rpc.common.dto.RpcRequest;
import com.jesper.rpc.common.dto.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc客户端，用于连接rpc服务端，向服务端发送请求
 * 主要是netty的模板代码
 */

public class RpcClient extends SimpleChannelInboundHandler<RpcResponse>{

    // RPC服务端的地址
    private String host;
    // RPC服务端的端口号
    private int port;
    // RPCResponse响应对象
    private RpcResponse response;
    // log4j日志记录
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 向RPC服务端发送请求方法
     *
     * @param request RPC客户端向RPC服务端发送的request对象
     * @return
     */
    public RpcResponse sendRequest(RpcRequest request) throws Exception {

        // 配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
                    // 设置TCP连接超时时间
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            //添加编码器，Rpc服务端需要解码的是RpcRequest对象，因为需要接收客户端发送过来的请求
                            sc.pipeline().addLast(new RpcDecoder(RpcResponse.class));
                            //添加解码器
                            sc.pipeline().addLast(new RpcEncoder());
                            //添加业务处理handler
                            sc.pipeline().addLast(RpcClient.this);
                        }
                    });
            // 发起异步连接操作（注意服务端是bind，客户端则需要connect）
            logger.info("准备发起异步连接操作[{}:{}]", host, port);
            ChannelFuture f = b.connect(host, port).sync();
            // 向RPC服务端发起请求
            logger.info("准备向RPC服务端发起请求...");
            f.channel().writeAndFlush(request);


            // 需要注意的是，如果没有接收到服务端返回数据，那么会一直停在这里等待
            // 等待客户端链路关闭
            logger.info("准备等待客户端链路关闭...");
            f.channel().closeFuture().sync();

        } finally {
            // 优雅退出，释放NIO线程组
            logger.info("优雅退出，释放NIO线程组...");
            group.shutdownGracefully();
        }
        return response;
    }


    /**
     * 读取rpc服务端的响应结果，并赋值给response对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        logger.info("从RPC服务端接收到响应...");
        this.response = rpcResponse;
        // 关闭与服务端的连接，这样就可以执行f.channel().closeFuture().sync();之后的代码，即优雅退出
        // 相当于是主动关闭连接
        ctx.close();
    }

}
