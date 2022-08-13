package com.github.artbits.quickio;

import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

final class DBHelper {

    private final static String DATABASE_NAME = "database";

    private static DB db;

    static void init() {
        init(null);
    }

    static void init(String path) {
        Tools.defer(DBHelper::destroy);
        try {
            DBFactory factory = new Iq80DBFactory();
            org.iq80.leveldb.Options options = new org.iq80.leveldb.Options();
            options.cacheSize(100 * 1024 * 1024);
            options.createIfMissing(true);
            path = (path != null) ? path + "/" + DATABASE_NAME : DATABASE_NAME;
            db = factory.open(new File(path), options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void destroy() {
        try {
            if (db != null) {
                db.close();
                db = null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean put(byte[] key, byte[] value) {
        try {
            db.put(key, value);
            return true;
        } catch (DBException e) {
            return false;
        }
    }

    static void writeBatch(Consumer<WriteBatch> consumer) {
        WriteBatch batch = db.createWriteBatch();
        try {
            consumer.accept(batch);
            db.write(batch);
        } catch (DBException e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] get(byte[] key) {
        try {
            return db.get(key);
        } catch (DBException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean delete(byte[] key) {
        try {
            db.delete(key);
            return true;
        } catch (DBException e) {
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
