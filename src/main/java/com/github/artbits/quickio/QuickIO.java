package com.github.artbits.quickio;

public final class QuickIO {

    public static Store store(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("The path parameter cannot be null or empty");
        }
        return new Store(path);
    }


    public static KV kv(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("The path parameter cannot be null or empty");
        }
        return new KV(path);
    }


    public static class Store extends QuickStore {
        Store(String path) {
            super(path);
        }
    }


    public static class KV extends QuickKV {
        KV(String path) {
            super(path);
        }
    }

}
