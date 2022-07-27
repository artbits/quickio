package com.github.artbits.quickio;

import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

final class DBHelper {

    private static DB db;

    static void init() {
        try {
            DBFactory factory = new Iq80DBFactory();
            org.iq80.leveldb.Options options = new org.iq80.leveldb.Options();
            options.cacheSize(100 * 1024 * 1024);
            options.createIfMissing(true);
            db = factory.open(new File("database"), options);
        } catch (IOException e) {
            destroy();
            throw new RuntimeException(e);
        }
    }

    static void destroy() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                db.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    static boolean put(byte[] key, byte[] value) {
        try {
            db.put(key, value);
            return true;
        } catch (DBException e) {
            destroy();
            return false;
        }
    }

    static void writeBatch(Consumer<WriteBatch> consumer) {
        WriteBatch batch = db.createWriteBatch();
        try {
            consumer.accept(batch);
            db.write(batch);
        } catch (DBException e) {
            destroy();
            throw new RuntimeException(e);
        }
    }

    static byte[] get(byte[] key) {
        try {
            return db.get(key);
        } catch (DBException e) {
            destroy();
            throw new RuntimeException(e);
        }
    }

    static boolean delete(byte[] key) {
        try {
            db.delete(key);
            return true;
        } catch (DBException e) {
            destroy();
            return false;
        }
    }

    static void iteration(BiConsumer<byte[], byte[]> consumer) {
        DBIterator iterator = db.iterator();
        for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            byte[] key = iterator.peekNext().getKey();
            byte[] value = iterator.peekNext().getValue();
            consumer.accept(key, value);
        }
    }

    static void iteration(BiPredicate<byte[], byte[]> predicate) {
        DBIterator iterator = db.iterator();
        for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            byte[] key = iterator.peekNext().getKey();
            byte[] value = iterator.peekNext().getValue();
            if (!predicate.test(key, value)) {
                break;
            }
        }
    }

    static <T> T iteration(BiFunction<byte[], byte[], T> function) {
        DBIterator iterator = db.iterator();
        for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
            byte[] key = iterator.peekNext().getKey();
            byte[] value = iterator.peekNext().getValue();
            T t = function.apply(key, value);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

}
