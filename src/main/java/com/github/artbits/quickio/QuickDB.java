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

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.artbits.quickio.Tools.asBytes;
import static com.github.artbits.quickio.Tools.asObject;

class QuickDB extends IO {

    QuickDB(String path) {
        super(path);
    }


    public <T extends QuickIO.Object> void save(T t) {
        if (t.id() == 0 || Tools.getDigit(t.id()) < 18) {
            t.id = QuickIO.id();
        }
        boolean bool = put(asBytes(t.id),  asBytes(t));
        if (!bool) {
            t.id = 0L;
        }
    }


    public <T extends QuickIO.Object> void save(List<T> list) {
        writeBatch(batch -> list.forEach(t -> {
            t.id = (t.id() == 0) ? QuickIO.id() : t.id();
            batch.put(asBytes(t.id), asBytes(t));
        }));
    }


    @SuppressWarnings("unchecked")
    public <T extends QuickIO.Object> void update(T t , Predicate<T> predicate) {
        Map<String, Field> tMap = Tools.getFields(t.getClass());
        tMap.remove("id");
        iteration((key, value) -> {
            T localT = (T) asObject(value, t.getClass());
            if (localT != null && predicate.test(localT)) {
                Map<String, Field> localTMap = Tools.getFields(localT.getClass());
                tMap.forEach((tFieldName, tField) -> {
                    Field localField = localTMap.getOrDefault(tFieldName, null);
                    Object tFieldValue = Tools.getFieldValue(t, tField);
                    if (localField != null && tFieldValue != null) {
                        Tools.setFieldValue(localT, localField, tFieldValue);
                    }
                });
                put(asBytes(localT.id()), asBytes(localT));
            }
        });
    }


    public boolean delete(long id) {
        return delete(asBytes(id));
    }


    public void delete(long... ids) {
        writeBatch(batch -> {
            for (long id : ids) {
                batch.delete(asBytes(id));
            }
        });
    }


    public <T extends QuickIO.Object> void delete(List<T> list) {
        writeBatch(batch -> list.forEach(t -> batch.delete(asBytes(t.id))));
    }


    public <T extends QuickIO.Object> void delete(Class<T> tClass, Predicate<T> predicate) {
        writeBatch(batch -> iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                batch.delete(key);
            }
        }));
    }


    public <T extends QuickIO.Object> void delete(Class<T> tClass) {
        delete(tClass, null);
    }


    public <T extends QuickIO.Object> T findFirst(Class<T> tClass, Predicate<T> predicate) {
        AtomicReference<T> minT = new AtomicReference<>();
        iteration((key, value) -> {
            long id = Tools.asLong(key);
            T t = asObject(value, tClass);
            if (t != null && (minT.get() == null || id < minT.get().id())) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                minT.set(t);
            }
        });
        return minT.get();
    }


    public <T extends QuickIO.Object> T findFirst(Class<T> tClass) {
        return findFirst(tClass, null);
    }


    public <T extends QuickIO.Object> T findLast(Class<T> tClass, Predicate<T> predicate) {
        AtomicReference<T> maxT = new AtomicReference<>();
        iteration((key, value) -> {
            long id = Tools.asLong(key);
            T t = asObject(value, tClass);
            if (t != null && (maxT.get() == null || id > maxT.get().id())) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                maxT.set(t);
            }
        });
        return maxT.get();
    }


    public <T extends QuickIO.Object> T findLast(Class<T> tClass) {
        return findLast(tClass, null);
    }


    public <T extends QuickIO.Object> T findOne(Class<T> tClass, Predicate<T> predicate) {
        return iteration((key, value) -> {
            T t = asObject(value, tClass);
            return t != null && predicate.test(t) ? t : null;
        });
    }


    public <T extends QuickIO.Object> List<T> find(Class<T> tClass, Predicate<T> predicate, Consumer<FindOptions> consumer) {
        FindOptions<T> options = (consumer != null) ? new FindOptions<>(tClass) : null;
        Optional.ofNullable(consumer).ifPresent(c -> c.accept(options));
        List<T> list = new ArrayList<>();
        iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                list.add(t);
            }
        });
        return (consumer != null) ? options.get(list) : list;
    }


    public <T extends QuickIO.Object> List<T> find(Class<T> tClass, Predicate<T> predicate) {
        return find(tClass, predicate, null);
    }


    public <T extends QuickIO.Object> List<T> find(Class<T> tClass) {
        return find(tClass, null, null);
    }


    public <T extends QuickIO.Object> List<T> find(Class<T> tClass, long... ids) {
        List<T> list = new ArrayList<>();
        for (long id : ids) {
            byte[] key = asBytes(id);
            byte[] value = get(key);
            T t = (value != null) ? asObject(value, tClass) : null;
            list.add(t);
        }
        return list;
    }


    public <T extends QuickIO.Object> T find(Class<T> tClass, long id) {
        byte[] key = asBytes(id);
        byte[] value = get(key);
        return (value != null) ? asObject(value, tClass) : null;
    }


    public <T extends QuickIO.Object> int count(Class<T> tClass, Predicate<T> predicate) {
        AtomicInteger count = new AtomicInteger(0);
        iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                count.incrementAndGet();
            }
        });
        return count.get();
    }


    public <T extends QuickIO.Object> int count(Class<T> tClass) {
        return count(tClass, null);
    }

}