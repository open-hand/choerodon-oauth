package io.choerodon.oauth.infra.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.Assert;

public class CustomJdkSerializationRedisSerializer implements RedisSerializer<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomJdkSerializationRedisSerializer.class);

    private final byte[] emptyArray = new byte[0];

    private final Converter<Object, byte[]> serializer;
    private final Converter<byte[], Object> deserializer;


    public CustomJdkSerializationRedisSerializer() {
        this(new SerializingConverter(), new DeserializingConverter());
    }

    public CustomJdkSerializationRedisSerializer(ClassLoader classLoader) {
        this(new SerializingConverter(), new DeserializingConverter(classLoader));
    }


    public CustomJdkSerializationRedisSerializer(Converter<Object, byte[]> serializer, Converter<byte[], Object> deserializer) {

        Assert.notNull(serializer, "Serializer must not be null!");
        Assert.notNull(deserializer, "Deserializer must not be null!");

        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public Object deserialize(byte[] bytes) {
        if (isEmpty(bytes)) {
            return null;
        }
        try {
            return deserializer.convert(bytes);
        } catch (Exception ex) {
            LOGGER.warn("Cannot deserialize {}", ex.getMessage());
        }
        return null;
    }

    public byte[] serialize(Object object) {
        if (object == null) {
            return emptyArray;
        }
        try {
            return serializer.convert(object);
        } catch (Exception ex) {
            throw new SerializationException("Cannot serialize", ex);
        }
    }

    private boolean isEmpty(byte[] data) {
        return (data == null || data.length == 0);
    }
}
