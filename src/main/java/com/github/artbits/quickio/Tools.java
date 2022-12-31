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

package com.github.artbits.quickio;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class Tools {

    static <T> T asObject(byte[] value, Class<T> tClass) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(value)) {
            Hessian2Input input = new Hessian2Input(bis);
            Object o = input.readObject();
            input.close();
            return o.getClass().getSimpleName().equals(tClass.getSimpleName()) ? tClass.cast(o) : null;
        } catch (IOException e) {
            return null;
        }
    }


    static byte[] asBytes(Object o) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Hessian2Output output = new Hessian2Output(bos);
            output.writeObject(o);
            output.flushBuffer();
            output.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static byte[] asBytes(String key) {
        return key.getBytes(StandardCharsets.UTF_8);
    }


    static byte[] asBytes(long key) {
        return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(key).array();
    }


    static long asLong(byte[] key) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(key, 0, key.length);
        buffer.flip();
        return buffer.getLong();
    }


    static Map<String, Field> getFields(Class<?> clazz) {
        Map<String, Field> map = new HashMap<>();
        while (clazz != null){
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                map.put(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }
        return map;
    }


    static Object getFieldValue(Object object, Field field) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    static void setFieldValue(Object object, Field field, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    static int getDigit(long i){
        i = i > 0 ? i : -i;
        return i == 0 ? 1 : (int) Math.log10(i) + 1;
    }


    static void print(Object o) {
        System.out.print(o);
    }


    static void println(Object o) {
        System.out.println(o);
    }


    static void println(String s, Object... args) {
        System.out.printf(s + "%n", args);
    }


    static <T> void printJson(T t) {
        QuickIO.println(Optional.ofNullable(t)
                        .map(s -> QuickIO.toJson(t))
                        .orElse("The converted JSON object cannot be null"));
    }

}