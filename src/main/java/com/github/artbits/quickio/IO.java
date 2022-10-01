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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

class IO {

    private final File file;
    private final DBFactory factory;
    private final DB db;


    IO(String path) {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        try {
            file = new File(path);
            factory = new Iq80DBFactory();
            Options options = new Options();
            options.createIfMissing(true);
            options.cacheSize(100 * 1024 * 1024);
            db = factory.open(file, options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void close() {
        try {
            if (db != null) {
                db.close();
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