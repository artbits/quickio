/**
 * Copyright 2022 Zhang Guanhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.artbits.quickio.core;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

final class Codec {

    private final static ConcurrentHashMap<Class<?>, byte[]> map = new ConcurrentHashMap<>();


    static byte[] encodeKey(long v) {
        return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(v).array();
    }


    static long decodeKey(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        return buffer.getLong();
    }


    @SuppressWarnings("unchecked")
    static <T> byte[] encode(T t) {
        byte[] bytes1 = getClassNameBytes(t.getClass());
        byte[] bytes2 = new byte[]{0};
        byte[] bytes3 = ProtobufIOUtil.toByteArray(t,
                RuntimeSchema.getSchema((Class<T>) t.getClass()),
                LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
        return ByteBuffer.allocate(bytes1.length + bytes2.length + bytes3.length)
                .put(bytes1)
                .put(bytes2)
                .put(bytes3)
                .array();
    }


    static <T> T decode(byte[] bytes, Class<T> clazz) {
        byte[] classNameBytes = getClassNameBytes(clazz);
        if (bytes == null || bytes.length <= classNameBytes.length) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] bytes1 = new byte[classNameBytes.length];
        byte[] bytes2 = new byte[]{0};
        buffer.get(bytes1, 0, bytes1.length);
        buffer.get(bytes2, 0, bytes2.length);
        if (bytes2[0] != 0) {
            return null;
        }
        if (!arraysEquals(bytes1, classNameBytes)) {
            return null;
        }
        byte[] bytes3 = new byte[bytes.length - bytes1.length - bytes2.length];
        buffer.get(bytes3, 0, bytes3.length);
        Schema<T> tSchema = RuntimeSchema.getSchema(clazz);
        T t = tSchema.newMessage();
        ProtobufIOUtil.mergeFrom(bytes3, t, tSchema);
        return t;
    }


    static <T> T clone(T t, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        byte[] bytes = ProtobufIOUtil.toByteArray(t, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
        T t1 = schema.newMessage();
        ProtobufIOUtil.mergeFrom(bytes, t1, schema);
        return t1;
    }


    static String getClassName(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            if (b == 0) {
                break;
            } else {
                stringBuilder.append((char) (int) b);
            }
        }
        return stringBuilder.toString();
    }


    private static byte[] getClassNameBytes(Class<?> clazz) {
        byte[] bytes = map.getOrDefault(clazz, null);
        if (bytes == null) {
            bytes = clazz.getSimpleName().getBytes(StandardCharsets.UTF_8);
            map.put(clazz, bytes);
        }
        return bytes;
    }


    private static boolean arraysEquals(byte[] a1, byte[] a2) {
        if (a1 == a2) {
            return true;
        }
        if (a1 == null || a2 == null) {
            return false;
        }
        int length = a1.length;
        if (a2.length != length) {
            return false;
        }
        for (int i = 0, size = length / 2; i <= size; i++) {
            if (a1[i] != a2[i] || a1[length - 1 - i] != a2[length - 1 - i]) {
                return false;
            }
        }
        return true;
    }

}