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

    private static final Stack<Runnable> deferStack = new Stack<>();
    private static final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private static final Hessian2Output output = new Hessian2Output(bos);

    private static ByteArrayInputStream bis;
    private static Hessian2Input input;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            while (!deferStack.empty()) {
                deferStack.pop().run();
            }
        }));

        defer(() -> {
            try {
                bos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        defer(() -> {
            try {
                output.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        defer(() -> {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        defer(() -> {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    static void defer(Runnable runnable) {
        if (runnable != null) {
            deferStack.push(runnable);
        }
    }

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
            output.writeObject(new Data(o));
            output.flushBuffer();
            byte[] bytes = bos.toByteArray();
            output.reset();
            bos.reset();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> T asObject(byte[] value, Class<T> tClass) {
        try {
            bis = new ByteArrayInputStream(value);
            input = new Hessian2Input(bis);
            Data data = (Data) input.readObject();
            input.reset();
            bis.reset();
            return tClass.cast(data.object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T getObject(Data data) {
        return (T) data.object;
    }

    static Data asData(byte[] value) {
        try {
            bis = new ByteArrayInputStream(value);
            input = new Hessian2Input(bis);
            Data data = (Data) input.readObject();
            input.reset();
            bis.reset();
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

    static Field getIDField(Class<?> clazz) {
        while (clazz != null){
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals("id")) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    static int getDigit(long i){
        i = i > 0 ? i : -i;
        return i == 0 ? 1 : (int) Math.log10(i) + 1;
    }

    static boolean equals(String s1, String s2) {
        if (s1 == null || s2 == null || s1.length() != s2.length()) {
            return false;
        }
        return s1.equals(s2);
    }

}
