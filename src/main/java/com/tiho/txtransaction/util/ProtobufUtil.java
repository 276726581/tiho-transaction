package com.tiho.txtransaction.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;

public final class ProtobufUtil {

    private ProtobufUtil() {

    }

    public static <T> T mergeFrom(byte[] data, T message, Schema<T> schema) {
        ProtobufIOUtil.mergeFrom(data, message, schema);
        return message;
    }

    public static <T> byte[] toByteArray(T message, Schema<T> schema) {
        byte[] bytes = ProtobufIOUtil.toByteArray(message, schema, LinkedBuffer.allocate());
        return bytes;
    }
}
