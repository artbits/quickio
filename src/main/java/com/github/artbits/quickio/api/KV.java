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

public interface KV extends AutoCloseable {
    @Override
    void close();
    void destroy();
    <K, V> void write(K key, V value);
    <K, V> V read(K key, V defaultValue);
    <K, V> V read(K key, Class<V> clazz);
    boolean erase(String key);
    boolean contains(String key);
}
