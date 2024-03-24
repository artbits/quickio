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

package com.github.artbits.quickio.api;

import com.github.artbits.quickio.core.Config;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface JKV extends AutoCloseable {
    @Override
    void close();
    void destroy();
    <K, V> void set(K key, V value);
    <K, V> V get(K key, V defaultValue);
    <K, V> V get(K key, Class<V> clazz);
    <K> boolean del(K key);
    <K> boolean exists(K key);
    <K> void rename(K oldKey, K newKey);
    <K> String type(K key);
    <K, V> void foreach(Class<K> kClass, Class<V> vClass, BiConsumer<K, V> consumer);
    <K, V> void foreach(Class<K> kClass, Class<V> vClass, BiFunction<K, V, Boolean> function);
}
