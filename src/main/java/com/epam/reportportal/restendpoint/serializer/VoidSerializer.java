package com.epam.reportportal.restendpoint.serializer;

import com.epam.reportportal.restendpoint.http.exception.SerializerException;
import com.google.common.net.MediaType;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * @author Andrei Varabyeu
 */
public class VoidSerializer implements Serializer {

    @Override
    public final <T> byte[] serialize(T t) throws SerializerException {
        throw new UnsupportedOperationException("Serialization is not permitted for Void types");
    }

    @Override
    public final <T> T deserialize(byte[] content, Class<T> clazz) throws SerializerException {
        return null;
    }

    @Override
    public final <T> T deserialize(byte[] content, Type type) throws SerializerException {
        return null;
    }

    @Override
    public final MediaType getMimeType() {
		throw new UnsupportedOperationException("Void type doesn't have mime type");
    }

    @Override
    public final boolean canRead(MediaType mimeType, Class<?> resultType) {
        return Void.class.equals(resultType);
    }

    @Override
    public final boolean canRead(MediaType mimeType, Type resultType) {
		TypeToken type = TypeToken.of(resultType);
		return Void.TYPE.equals(type.getType()) || canRead(mimeType, type.getRawType());
	}

    @Override
    public final boolean canWrite(Object o) {
        return false;
    }
}
