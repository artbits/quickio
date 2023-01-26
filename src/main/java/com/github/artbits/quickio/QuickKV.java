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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.artbits.quickio.Tools.asBytes;
import static com.github.artbits.quickio.Tools.asObject;

class QuickKV extends LevelIO {

    private final QuickIO.Options options;


    QuickKV(Consumer<QuickIO.Options> consumer) {
        options = new QuickIO.Options();
        consumer.accept(options);
        if (options.basePath == null) {
            options.outBasePath = Constants.OUT_KV_PATH;
            options.basePath = Constants.KV_PATH;
        } else {
            options.outBasePath = (options.basePath + "/" + Constants.OUT_KV_PATH).replaceAll("//", "/");
            options.basePath = (options.basePath + "/" + Constants.KV_PATH).replaceAll("//", "/");
        }
        open(options);
    }


    QuickKV(String name) {
        this(options -> options.name = name);
    }


    public <V> void write(String key, V value) {
        put(asBytes(key), asBytes(value));
    }


    @SuppressWarnings("unchecked")
    public <V> V read(String key, V defaultValue) {
        byte[] bytes = get(asBytes(key));
        if (bytes == null) {
            return defaultValue;
        } else if (defaultValue instanceof Character) {
            Object object = asObject(bytes, String.class);
            Character character = String.valueOf(object).charAt(0);
            return (object == null) ? defaultValue : (V) character;
        } else {
            Object object = asObject(bytes, defaultValue.getClass());
            return (object == null) ? defaultValue : (V) object;
        }
    }


    public <T> T read(String key, Class<T> tClass) {
        byte[] bytes = get(asBytes(key));
        return (bytes != null) ? asObject(bytes, tClass) : null;
    }


    public boolean remove(String key) {
        delete(asBytes(key));
        return true;
    }


    public boolean containsKey(String key) {
        byte[] bytes = get(asBytes(key));
        return bytes != null;
    }


    public void export(Consumer<String> consumer1, Consumer<Exception> consumer2) {
        String basePath = String.format("%s%s/", options.outBasePath, options.name);
        String fileName = String.format("%s_%d.txt", options.name, System.currentTimeMillis());
        Exporter exporter = new Exporter(basePath, fileName);
        StringBuilder builder = new StringBuilder();
        iteration((key, value) -> {
            String s = new String(key);
            Object o = asObject(value);
            if (o != null) {
                Exporter.KVObject kvObject = new Exporter.KVObject(s, o);
                builder.append(new JSONObject(kvObject)).append("\n");
            }
        });
        exporter.exportFile(builder.toString(), consumer1, consumer2);
    }


    public static void open(String name, Consumer<QuickIO.KV> consumer1, Consumer<Exception> consumer2) {
        try (QuickIO.KV kv = new QuickIO.KV(name)) {
            consumer1.accept(kv);
        } catch (Exception e) {
            Optional.ofNullable(consumer2)
                    .orElseThrow(() -> new RuntimeException(e))
                    .accept(e);
        }
    }


    public static void open(String name, Consumer<QuickIO.KV> consumer) {
        open(name, consumer, null);
    }


    public static <T> T openGet(String name, Function<QuickIO.KV, T> function, Consumer<Exception> consumer) {
        try (QuickIO.KV kv = new QuickIO.KV(name)) {
            return function.apply(kv);
        } catch (Exception e) {
            Optional.ofNullable(consumer)
                    .orElseThrow(() -> new RuntimeException(e))
                    .accept(e);
            return null;
        }
    }


    public static <T> T openGet(String name, Function<QuickIO.KV, T> function) {
        return openGet(name, function, null);
    }

}