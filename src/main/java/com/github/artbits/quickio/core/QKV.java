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

import com.github.artbits.quickio.api.KV;
import com.github.artbits.quickio.exception.QIOException;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import static com.github.artbits.quickio.core.Constants.KV_PATH;

final class QKV implements KV {

    private final EngineIO engine;


    QKV(Config config) {
        if (config.path == null) {
            config.path = KV_PATH;
        } else {
            config.path = Paths.get(config.path, KV_PATH).toAbsolutePath().toString();
        }
        engine = new EngineLevel().open(config);
    }


    QKV(String name) {
        this(Config.of(c -> c.name(name)));
    }


    @Override
    public void close() {
        engine.close();
    }

    @Override
    public void destroy() {
        engine.destroy();
    }


    @Override
    public <K, V> void write(K key, V value) {
        engine.put(Codec.encode(key), Codec.encode(value));
    }


    @SuppressWarnings("unchecked")
    @Override
    public <K, V> V read(K key, V defaultValue) {
        byte[] bytes = engine.get(Codec.encode(key));
        if (bytes != null) {
            Object object = Codec.decode(bytes, defaultValue.getClass());
            return (object == null) ? defaultValue : (V) object;
        }
        return defaultValue;
    }


    @Override
    public <K, V> V read(K key, Class<V> clazz) {
        byte[] bytes = engine.get(Codec.encode(key));
        if (bytes != null) {
            Object object = Codec.decode(bytes, clazz);
            return (object != null) ? clazz.cast(object) : null;
        }
        return null;
    }


    @Override
    public <K> boolean erase(K key) {
        engine.delete(Codec.encode(key));
        return true;
    }


    @Override
    public <K> boolean contains(K key) {
        byte[] bytes = engine.get(Codec.encode(key));
        return bytes != null;
    }


    @Override
    public <K> void rename(K oldKey, K newKey) {
        byte[] oldKeyBytes = Codec.encode(oldKey);
        byte[] newKeyBytes = Codec.encode(newKey);
        if (Arrays.equals(oldKeyBytes, newKeyBytes)) {
            return;
        }
        if (engine.get(newKeyBytes) != null) {
            throw new QIOException(Constants.KEY_ALREADY_EXISTS_AND_NOT_AVAILABLE);
        }
        byte[] valueBytes = engine.get(oldKeyBytes);
        Optional.ofNullable(valueBytes).ifPresent(bytes -> engine.writeBatch(batch -> {
            engine.put(newKeyBytes, valueBytes);
            engine.delete(oldKeyBytes);
        }));
    }


    @Override
    public <K> String type(K key) {
        byte[] bytes = engine.get(Codec.encode(key));
        return (bytes != null) ? Codec.getClassName(bytes) : null;
    }

}