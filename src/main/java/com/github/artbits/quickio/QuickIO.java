package com.github.artbits.quickio;

import java.io.Serializable;

public final class QuickIO {

    private final static String DB_PATH = "data/db/";
    private final static String KV_PATH = "data/kv/";
    private final static String CAN_PATH = "data/can/";
    private final static String EXCEPTION_MESSAGE = "The parameter cannot be null or empty";


    public static class Object implements Serializable {
        long id;

        public final long id() {
            return id;
        }

        public final long timestamp() {
            return Snowflake.timestamp(id);
        }
    }


    public static class DB extends QuickDB {
        DB(String path) {
            super(path);
        }
    }


    public static class KV extends QuickKV {
        KV(String path) {
            super(path);
        }
    }


    public static class Can extends QuickCan {
        Can(String path) {
            super(path);
        }
    }


    public static DB db(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }
        return new DB(DB_PATH + path);
    }


    public static KV kv(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }
        return new KV(KV_PATH + path);
    }


    public static Can can(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }
        return new Can(CAN_PATH + path);
    }

}