package com.jesper.rpc.common.utils;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SerializationUtil:具备缓存功能的序列化工具类，原生的序列化性能效率较低，产生的码流较大，所以采用了Protostuff实现
 * <p>
 */
public class SerializationUtil {

    /**
     * 构建schema的过程可能会比较耗时，因此将使用过的类对应的schema缓存起来
     * 1、序列化：通过对象的类构建对应的schema，使用schema将对象序列化为一个byte数组
     * 2、反序列化：通过对象的类构建对应的schema，使用schema将byte数组和对象合并
     */

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private static Objenesis objenesis = new ObjenesisStd(true);

    private static <T> Schema<T> getSchema(Class<T> clazz) {// 这里的clazz代表的是一个类型参数
        //根据类型从缓存里获取schema
        Schema<T> schema = (Schema<T>) cachedSchema.get(clazz);
        //若没有就从RuntimeSchema获取该该类型的schema，并添加到缓存
        if (schema == null) {
            schema = RuntimeSchema.getSchema(clazz);
            if (schema != null) {
                cachedSchema.put(clazz, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化  Java Object转成byte[]
     */
    public static <T> byte[] serializer(T obj) {
        //获取泛型对象的类型
        Class<T> clazz = (Class<T>) obj.getClass();
        // 创建LinkedBuffer对象
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clazz);
            // 序列化,并返回序列化对象
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化 将byte[]转成Java Object
     */
    public static <T> T deserializer(byte[] data, Class<T> clazz) {
        try {
            // 通过objenesis根据泛型对象实例化对象
            T obj = objenesis.newInstance(clazz);
            // 获取泛型对象的schema对象
            Schema<T> schema = getSchema(clazz);
            // 将字节数组中的数据反序列化到message对象
            ProtostuffIOUtil.mergeFrom(data, obj, schema);
            // 返回反序列化对象
            return obj;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
