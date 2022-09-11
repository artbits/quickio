package com.github.artbits.quickio;

public final class QuickIO {

    public static Store store(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("The parameter cannot be null or empty");
        }
        return new Store("data/store/" + path);
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


    public static class Can extends QuickCan {
        Can(String path) {
            super(path);
        }
    }

}
