package com.github.artbits.quickio;

import org.iq80.leveldb.*;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.github.artbits.quickio.Tools.defer;

class IO {

    private final File file;
    private final DBFactory factory;
    private DB db;


    IO(String path) {
        defer(this::close);
        try {
            Options options = new Options();
            options.cacheSize(100 * 1024 * 1024);
            options.createIfMissing(true);
            file = new File(path);
            factory = new Iq80DBFactory();
            db = factory.open(file, options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


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
            this.close();
            Files.walk(Paths.get(file.getPath()))
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
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
        WriteBatch batch = db.createWriteBatch();
        try {
            consumer.accept(batch);
            db.write(batch);
        } catch (DBException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                batch.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    void iteration(BiConsumer<byte[], byte[]> consumer) {
        DBIterator iterator = db.iterator();
        try {
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                byte[] key = iterator.peekNext().getKey();
                byte[] value = iterator.peekNext().getValue();
                consumer.accept(key, value);
            }
        } finally {
            try {
                iterator.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    <T> T iteration(BiFunction<byte[], byte[], T> function) {
        DBIterator iterator = db.iterator();
        try {
            for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                byte[] key = iterator.peekNext().getKey();
                byte[] value = iterator.peekNext().getValue();
                T t = function.apply(key, value);
                if (t != null) {
                    return t;
                }
            }
            return null;
        } finally {
            try {
                iterator.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}