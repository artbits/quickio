package com.github.artbits.quickio;

import static com.github.artbits.quickio.Tools.asBytes;
import static com.github.artbits.quickio.Tools.asObject;

class QuickKV extends IO {

    QuickKV(String path) {
        super(path);
    }


    public <V> void write(String key, V value) {
        put(asBytes(key), asBytes(value));
    }


    @SuppressWarnings("unchecked")
    public <V> V read(String key, V defaultValue) {
        byte[] bytes = get(asBytes(key));
        if (bytes == null || defaultValue == null) {
            return defaultValue;
        } else if (defaultValue instanceof Character) {
            Object object = asObject(bytes, String.class);
            Character character = String.valueOf(object).charAt(0);
            return (object == null) ? defaultValue : (V) character;
        } else {
            Object object = asObject(bytes, defaultValue.getClass());
            return (object == null) ? defaultValue : (V) object;
        }
    }


    public <T> T read(String key, Class<T> tClass) {
        byte[] bytes = get(asBytes(key));
        return (bytes != null) ? asObject(bytes, tClass) : null;
    }


    public boolean remove(String key) {
        return delete(asBytes(key));
    }


    public boolean containsKey(String key) {
        byte[] bytes = get(asBytes(key));
        return bytes != null;
    }

}