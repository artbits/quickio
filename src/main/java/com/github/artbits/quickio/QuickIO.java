package com.github.artbits.quickio;

public final class QuickIO {

    public static DB db(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("The parameter cannot be null or empty");
        }
        return new DB("data/db/" + path);
    }


    public static KV kv(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("The parameter cannot be null or empty");
        }
        return new KV("data/kv/" + path);
    }


    public static Can can(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("The parameter cannot be null or empty");
        }
        return new Can("data/can/" + path);
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

}
