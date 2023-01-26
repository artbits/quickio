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

import org.iq80.leveldb.*;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

class LevelIO implements AutoCloseable {

    private File file;
    private DBFactory factory;
    private DB db;
    private Runnable closeRunnable;


    void open(QuickIO.Options options) {
        if (options.name == null || options.name.isEmpty()) {
            throw new RuntimeException("The name cannot be null or empty");
        } else if (options.name.contains("/")) {
            throw new RuntimeException("Name cannot contain \"/\"");
        }
        if (options.cacheSize == null || options.cacheSize <= 0) {
            options.cacheSize = 100L * 1024 * 1024;
        }
        try {
            file = new File(options.basePath + options.name);
            factory = new Iq80DBFactory();
            db = factory.open(file, new Options().createIfMissing(true).cacheSize(options.cacheSize));
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() {
        try {
            if (db != null) {
                db.close();
                db = null;
                Optional.ofNullable(closeRunnable).ifPresent(Runnable::run);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


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
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    void closeListener(Runnable runnable) {
        this.closeRunnable = runnable;
    }


    void put(byte[] key, byte[] value, Consumer<DBException> consumer) {
        try {
            db.put(key, value);
        } catch (DBException e) {
            Optional.ofNullable(consumer)
                    .orElseThrow(() -> new RuntimeException(e))
                    .accept(e);
            throw new RuntimeException(e);
        }
    }


    void put(byte[] key, byte[] value) {
        put(key, value, null);
    }


    byte[] get(byte[] key) {
        try {
            return db.get(key);
        } catch (DBException e) {
            throw new RuntimeException(e);
        }
    }


    void delete(byte[] key) {
        try {
            db.delete(key);
        } catch (DBException e) {
            throw new RuntimeException(e);
        }
    }


    void writeBatch(Consumer<WriteBatch> consumer1, Consumer<Exception> consumer2) {
        try (WriteBatch batch = db.createWriteBatch()) {
            consumer1.accept(batch);
            db.write(batch);
        } catch (Exception e) {
            Optional.ofNullable(consumer2)
                    .orElseThrow(() -> new RuntimeException(e))
                    .accept(e);
            throw new RuntimeException(e);
        }
    }


    void writeBatch(Consumer<WriteBatch> consumer) {
        writeBatch(consumer, null);
    }


    void iteration(BiConsumer<byte[], byte[]> consumer) {
        try (DBIterator iterator = db.iterator()) {
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                byte[] key = iterator.peekNext().getKey();
                byte[] value = iterator.peekNext().getValue();
                consumer.accept(key, value);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    <T> T iteration(BiFunction<byte[], byte[], T> function) {
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
            throw new RuntimeException(e);
        }
    }

}