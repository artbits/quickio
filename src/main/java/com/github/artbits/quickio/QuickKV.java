/**
 * Copyright 2022 Zhang Guanhu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        if (bytes == null) {
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