package com.github.artbits.quickio;

public final class QuickIO {

    public static QuickStore store(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("The path parameter cannot be null or empty");
        }
        return new QuickStore(path);
    }


    public static QuickKV kv(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("The path parameter cannot be null or empty");
        }
        return new QuickKV(path);
    }


    public static void destroy(IO io) {
        io.destroy();
    }

}
