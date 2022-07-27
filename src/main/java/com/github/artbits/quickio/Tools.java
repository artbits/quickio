package com.github.artbits.quickio;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

final class Tools {

    static byte[] asByteArray(long value) {
        return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(value).array();
    }

    static long asLong(byte[] value) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(value, 0, value.length);
        buffer.flip();
        return buffer.getLong();
    }

    static byte[] asByteArray(Object o) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Hessian2Output output = new Hessian2Output(bos);
            output.writeObject(new Data(o));
            output.flushBuffer();
            byte[] bytes = bos.toByteArray();
            bos.close();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> T asObject(byte[] value, Class<T> tClass) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(value);
            Hessian2Input input = new Hessian2Input(bis);
            Data data = (Data) input.readObject();
            input.close();
            return tClass.cast(data.getObject());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Data asData(byte[] value) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(value);
            Hessian2Input input = new Hessian2Input(bis);
            Data data = (Data) input.readObject();
            input.close();
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Map<String, Field> getFields(Class<?> clazz) {
        Map<String, Field> map = new HashMap<>();
        while (clazz != null){
            for (Field field : clazz.getDeclaredFields()) {
                map.put(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }
        return map;
    }

    static int getDigit(long i){
        i = i > 0 ? i : -i;
        return i == 0 ? 1 : (int) Math.log10(i) + 1;
    }

}
