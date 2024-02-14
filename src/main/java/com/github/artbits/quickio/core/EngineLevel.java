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

import com.github.artbits.quickio.exception.QIOException;
import org.iq80.leveldb.api.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

final class EngineLevel implements EngineIO {

    private File file;
    private DBFactory factory;
    private DB db;


    @Override
    public EngineIO open(Config config) {
        if (config.name == null || config.name.isEmpty()) {
            throw new QIOException(Constants.ILLEGAL_NAME);
        } else if (config.name.contains("/")) {
            throw new QIOException(Constants.SPECIAL_CHARACTER_NAME);
        }
        if (config.cacheSize == null || config.cacheSize <= 0) {
            config.cacheSize = 10L * 1024 * 1024;
        }
        try {
            file = Paths.get(config.path, config.name).toFile();
            factory = new Iq80DBFactory();
            db = factory.open(file, new Options().createIfMissing(true).cacheSize(config.cacheSize));
        } catch (IOException e) {
            throw new QIOException(e);
        }
        return this;
    }


    @Override
    public void close() {
        try {
            if (db != null) {
                db.close();
                db = null;
            }
        } catch (IOException e) {
            throw new QIOException(e);
        }
    }


    @Override
    public void destroy() {
        try {
            factory.destroy(file, null);
            close();
            Path filePath = Paths.get(file.getPath());
            Comparator<Path> comparator = Comparator.reverseOrder();
            Files.walk(filePath).sorted(comparator).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new QIOException(e);
                }
            });
        } catch (IOException e) {
            throw new QIOException(e);
        }
    }


    @Override
    public void put(byte[] key, byte[] value) {
        db.put(key, value);
    }


    @Override
    public void delete(byte[] key) {
        db.delete(key);
    }


    @Override
    public byte[] get(byte[] key) {
        return db.get(key);
    }


    @Override
    public void writeBatch(Consumer<WriteBatch> consumer) {
        try (WriteBatch batch = db.createWriteBatch()) {
            consumer.accept(batch);
            db.write(batch);
        } catch (IOException e) {
            throw new QIOException(e);
        }
    }


    @Override
    public void iteration(BiConsumer<byte[], byte[]> consumer) {
        try (DBIterator iterator = db.iterator()) {
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                byte[] key = iterator.peekNext().getKey();
                byte[] value = iterator.peekNext().getValue();
                consumer.accept(key, value);
            }
        } catch (IOException e) {
            throw new QIOException(e);
        }
    }


    @Override
    public <T> T iteration(BiFunction<byte[], byte[], T> function) {
        try (DBIterator iterator = db.iterator()) {
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                byte[] key = iterator.peekNext().getKey();
                byte[] value = iterator.peekNext().getValue();
                T t = function.apply(key, value);
                if (t != null) {
                    return t;
                }
            }
            return null;
        } catch (IOException e) {
            throw new QIOException(e);
        }
    }

}