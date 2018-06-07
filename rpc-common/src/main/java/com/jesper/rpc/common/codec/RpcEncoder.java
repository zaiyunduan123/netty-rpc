package com.jesper.rpc.common.codec;

import com.jesper.rpc.common.utils.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jiangyunxiong on 2018/6/1.
 * <p>
 * 编码器：负责将Object类型的POJO对象编码为byte数组，然后写入到ByteBuf中 ，底层实现是序列化
 */
public class RpcEncoder extends MessageToByteEncoder<Object> {

    private Class<?> genericClass;

    private Logger logger = LoggerFactory.getLogger(RpcEncoder.class);

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    public RpcEncoder() {

    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            logger.info("准备序列化对象...");
            byte[] data = SerializationUtil.serializer(o);
            logger.info("序列化对象完毕，准备将其写入到ByteBuf中...");
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
    }
}
