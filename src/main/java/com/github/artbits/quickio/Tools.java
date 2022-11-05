package com.github.artbits.quickio;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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


    static <T extends QuickIO.Object> String toJson(T t) {
        try {
            Map<String, Object> jsonMap = new HashMap<>();
            Map<String, Field> fieldMap = getFields(t.getClass());
            for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
                jsonMap.put(entry.getKey(), entry.getValue().get(t));
            }
            return new JSONObject(jsonMap).toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    static int getDigit(long i){
        i = i > 0 ? i : -i;
        return i == 0 ? 1 : (int) Math.log10(i) + 1;
    }

}