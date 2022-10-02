package com.github.artbits.quickio;

import java.io.Serializable;

public final class QuickIO {

    private final static String DB_PATH = "data/db/";
    private final static String KV_PATH = "data/kv/";
    private final static String CAN_PATH = "data/can/";
    private final static Snowflake snowflake = new Snowflake(0, 0);


    public static class Object implements Serializable {
        long id;

        public final long id() {
            return id;
        }

        public final long timestamp() {
            return toTimestamp(id);
        }
    }


    public static class DB extends QuickDB {
        public DB(String path) {
            super(DB_PATH + path);
        }
    }


    public static class KV extends QuickKV {
        public KV(String path) {
            super(KV_PATH + path);
        }
    }


    public static class Can extends QuickCan {
        public Can(String path) {
            super(CAN_PATH + path);
        }
    }


    public static long id() {
        return snowflake.nextId();
    }


    public static long toTimestamp(long id) {
        return snowflake.toTimestamp(id);
    }

}