package com.github.artbits.quickio;

import static com.github.artbits.quickio.Tools.asBytes;
import static com.github.artbits.quickio.Tools.asObject;

public class QuickKV extends IO {

    QuickKV(String path) {
        super("data/kv/" + path);
    }


    public <V> void write(String key, V value) {
        put(asBytes(key), asBytes(value));
    }


    @SuppressWarnings("unchecked")
    public <V> V read(String key, V defaultValue) {
        byte[] bytes = get(asBytes(key));
        if (bytes == null) {
            return defaultValue;
        } else if (defaultValue instanceof Character) {
            Object object = asObject(bytes, String.class);
            String s = String.valueOf(object);
            Character character = s.charAt(0);
            return object == null ? defaultValue : (V) character;
        } else {
            try {
                Object object = asObject(bytes, defaultValue.getClass());
                return object == null ? defaultValue : (V) object;
            } catch (NullPointerException e) {
                return null;
            }
        }
    }


    public <T> T read(String key, Class<T> tClass) {
        byte[] bytes = get(asBytes(key));
        return (bytes != null) ? asObject(bytes, tClass) : null;
    }

}
