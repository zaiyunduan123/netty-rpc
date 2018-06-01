package com.jesper.rpc.common.codec;

import com.jesper.rpc.common.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by jiangyunxiong on 2018/6/1.
 * <p>
 * 解码器 : 负责解码网络上过来的数据,从网络缓冲区读取的字节转换成有意义的消息对象的 ，底层实现是反序列化
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Logger logger = LoggerFactory.getLogger(RpcDecoder.class);

    //需要反序列对象的类型
    private Class<?> genericClass;

    // 构造方法，传入需要反序列化对象的类型
    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //ByteToMessageDecoder可能出现半包问题，定义4个字节来存储，字节小于4被认为是半包先不读，下次再读取
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        // ByteBuf的长度
        int length = byteBuf.readInt();
        if (length < 0)
            channelHandlerContext.close();
        if (byteBuf.readableBytes() < length)
            byteBuf.resetReaderIndex();

        // 1、构建length长度的字节数组
        byte[] data = new byte[length];
        // 2、将ByteBuf数据复制到字节数组中
        byteBuf.readBytes(data);
        logger.info("准备反序列化对象...");
        // 3、进行反序列化对象
        Object obj = SerializationUtil.deserializer(data, genericClass);
        logger.info("反序列化对象完毕，准备将其添加到反序列化对象结果列表...");
        // 4、添加到反序列化对象结果列表
        list.add(obj);
    }
}
