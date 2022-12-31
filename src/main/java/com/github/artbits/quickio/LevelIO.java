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

    private final File file;
    private final DBFactory factory;
    private DB db;


    LevelIO(String path) {
        try {
            Optional.ofNullable(path).orElseThrow(() -> new RuntimeException("The path cannot be null or empty"));
            file = new File(path);
            factory = new Iq80DBFactory();
            Options options = new Options();
            options.createIfMissing(true);
            options.cacheSize(100 * 1024 * 1024);
            db = factory.open(file, options);
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


    boolean put(byte[] key, byte[] value) {
        try {
            db.put(key, value);
            return true;
        } catch (DBException e) {
            return false;
        }
    }


    byte[] get(byte[] key) {
        try {
            return db.get(key);
        } catch (DBException e) {
            throw new RuntimeException(e);
        }
    }


    boolean delete(byte[] key) {
        try {
            db.delete(key);
            return true;
        } catch (DBException e) {
            return false;
        }
    }


    void writeBatch(Consumer<WriteBatch> consumer) {
        try (WriteBatch batch = db.createWriteBatch()) {
            consumer.accept(batch);
            db.write(batch);
        } catch (DBException | IOException e) {
            throw new RuntimeException(e);
        }
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