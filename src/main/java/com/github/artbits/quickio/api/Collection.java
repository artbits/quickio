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

import com.github.artbits.quickio.core.IOEntity;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Collection<T extends IOEntity> {
    void save(final T t);
    void save(final List<T> list);
    void update(T t , Predicate<T> predicate);
    void delete(long id);
    void delete(long... ids);
    void delete(List<Long> ids);
    void delete(Predicate<T> predicate);
    void deleteAll();
    List<T> findAll();
    List<T> find(Predicate<T> predicate, Consumer<FindOptions> consumer);
    List<T> find(Predicate<T> predicate);
    List<T> find(List<Long> ids);
    List<T> find(long... ids);
    List<T> findWithID(Predicate<Long> predicate, Consumer<FindOptions> consumer);
    List<T> findWithID(Predicate<Long> predicate);
    List<T> findWithTime(Predicate<Long> predicate, Consumer<FindOptions> consumer);
    List<T> findWithTime(Predicate<Long> predicate);
    T findFirst(Predicate<T> predicate);
    T findFirst();
    T findLast(Predicate<T> predicate);
    T findLast();
    T findOne(long id);
    T findOne(Predicate<T> predicate);
    T findWithIndex(Consumer<FindOptions> consumer);
    boolean exist(Consumer<FindOptions> consumer);
    void dropIndex(String fieldName);
    long count(Predicate<T> predicate);
    long count();
}
