package com.github.artbits.quickio;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
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


    static <T> T asObject(byte[] value, Class<T> tClass) {
        try {
            bis = new ByteArrayInputStream(value);
            input = new Hessian2Input(bis);
            Object o = input.readObject();
            input.reset();
            bis.reset();
            return o.getClass().getSimpleName().equals(tClass.getSimpleName()) ? tClass.cast(o) : null;
        } catch (Exception e) {
            return null;
        }
    }


    static byte[] asBytes(Object o) {
        try {
            output.writeObject(o);
            output.flushBuffer();
            byte[] bytes = bos.toByteArray();
            output.reset();
            bos.reset();
            return bytes;
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


    static void closeFileChannel(FileChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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


    static int getDigit(long i){
        i = i > 0 ? i : -i;
        return i == 0 ? 1 : (int) Math.log10(i) + 1;
    }

}
