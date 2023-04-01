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

package com.github.artbits.quickio.struct;

import com.google.common.collect.HashBiMap;

import java.util.function.BiConsumer;

public final class BiMap<K, V> {

    private final com.google.common.collect.BiMap<K, V> biMap = HashBiMap.create();


    public BiMap<K, V> put(K key, V value) {
        biMap.put(key, value);
        return this;
    }


    public V forcePut(K key, V value) {
        return biMap.forcePut(key, value);
    }


    public V getValue(K key) {
        return biMap.get(key);
    }


    public V getValue(K key, V defaultValue) {
        return biMap.getOrDefault(key, defaultValue);
    }


    public K getKey(V value) {
        return biMap.inverse().get(value);
    }


    public K getKey(V value, K defaultKey) {
        return biMap.inverse().getOrDefault(value, defaultKey);
    }


    public void remove(K key) {
        biMap.remove(key);
    }


    public void remove(K key, V value) {
        biMap.remove(key, value);
    }


    public void forEach(BiConsumer<K, V> consumer) {
        biMap.forEach(consumer);
    }

}