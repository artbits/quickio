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

package com.github.artbits.quickio.core;

import com.github.artbits.quickio.api.Collection;
import com.github.artbits.quickio.api.FindOptions;
import com.github.artbits.quickio.exception.QIOException;
import org.iq80.leveldb.DBException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;


final class QCollection<T extends IOEntity> implements Collection<T> {

    private final EngineIO engine;
    private final Indexer indexer;
    private final Class<T> clazz;


    QCollection(Class<T> clazz, EngineIO engine, Indexer indexer) {
        this.clazz = clazz;
        this.engine = engine;
        this.indexer = indexer;
    }


    @Override
    public void save(T t) {
        if (t.objectId() == 0 || Utility.getDigit(t.objectId()) < 18) {
            t._id = Plugin.generateId();
            t.createdAt = Plugin.toTimestamp(t.objectId());
        }
        indexer.setIndex(t);
        try {
            engine.put(Codec.encodeKey(t.objectId()), Codec.encode(t));
        } catch (DBException e) {
            indexer.removeIndex(t);
            throw new QIOException(e);
        }
    }


    @Override
    public void save(List<T> list) {
        list.forEach(t -> {
            if (t.objectId() == 0 || Utility.getDigit(t.objectId()) < 18) {
                t._id = Plugin.generateId();
                t.createdAt = Plugin.toTimestamp(t.objectId());
            }
        });
        indexer.setIndexes(list);
        try {
            engine.writeBatch(batch -> list.forEach(t -> batch.put(Codec.encodeKey(t.objectId()), Codec.encode(t))));
        } catch (Exception e) {
            indexer.removeIndexList(list);
            throw new QIOException(e);
        }
    }


    @Override
    public void update(T t, Predicate<T> predicate) {
        List<T> newLocalTList = new ArrayList<>();
        List<T> oldLocalTList = new ArrayList<>();
        Map<String, Field> tMap = Utility.getFields(t.getClass());
        tMap.remove("_id");
        tMap.remove("createdAt");
        engine.iteration((key, value) -> {
            T localT = Codec.decode(value, clazz);
            if (localT != null && predicate.test(localT)) {
                oldLocalTList.add(Codec.clone(localT, clazz));
                Map<String, Field> localTMap = Utility.getFields(localT.getClass());
                tMap.forEach((tFieldName, tField) -> {
                    Field localField = localTMap.getOrDefault(tFieldName, null);
                    Object tFieldValue = Utility.getFieldValue(t, tField);
                    if (localField != null && tFieldValue != null) {
                        Utility.setFieldValue(localT, localField, tFieldValue);
                    }
                });
                newLocalTList.add(localT);
            }
        });
        indexer.setIndexes(newLocalTList);
        try {
            engine.writeBatch(batch -> newLocalTList.forEach(t1 -> batch.put(Codec.encodeKey(t1.objectId()), Codec.encode(t1))));
        } catch (Exception e) {
            indexer.removeIndexList(newLocalTList);
            indexer.setIndexes(oldLocalTList);
            throw new RuntimeException(e);
        }
    }


    @Override
    public void updateWithIndex(T t, Consumer<FindOptions> consumer) {
        T localT = findWithIndex(consumer);
        if (localT != null) {
            Map<String, Field> localTMap = Utility.getFields(localT.getClass());
            Map<String, Field> tMap = Utility.getFields(t.getClass());
            tMap.remove("_id");
            tMap.remove("createdAt");
            tMap.forEach((tFieldName, tField) -> {
                Field localField = localTMap.getOrDefault(tFieldName, null);
                Object tFieldValue = Utility.getFieldValue(t, tField);
                if (localField != null && tFieldValue != null) {
                    Utility.setFieldValue(localT, localField, tFieldValue);
                }
            });
            save(localT);
        }
    }


    @Override
    public void delete(long id) {
        engine.delete(Codec.encodeKey(id));
        indexer.removeIndex(id);
    }


    @Override
    public void delete(long... ids) {
        engine.writeBatch(batch -> {
            for (long id : ids) {
                batch.delete(Codec.encodeKey(id));
            }
        });
        indexer.removeIndexes(ids);
    }


    @Override
    public void delete(List<Long> ids) {
        engine.writeBatch(batch -> ids.forEach(id -> batch.delete(Codec.encodeKey(id))));
        indexer.removeIndexes(ids);
    }


    @Override
    public void delete(Predicate<T> predicate) {
        List<Long> ids = new ArrayList<>();
        engine.writeBatch(batch -> engine.iteration((key, value) -> {
            T t = Codec.decode(value, clazz);
            if (t != null) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                batch.delete(key);
                ids.add(t.objectId());
            }
        }));
        indexer.removeIndexes(ids);
    }


    @Override
    public void deleteAll() {
        delete((Predicate<T>) null);
    }


    @Override
    public void deleteWithIndex(Consumer<FindOptions> consumer) {
        T t = findWithIndex(consumer);
        Optional.ofNullable(t).ifPresent(t1 -> delete(t1.objectId()));
    }


    @Override
    public List<T> findAll() {
        return find(null, null);
    }


    @Override
    public List<T> find(Predicate<T> predicate, Consumer<FindOptions> consumer) {
        QFindOptions options = (consumer != null) ? new QFindOptions() : null;
        Optional.ofNullable(consumer).ifPresent(c -> c.accept(options));
        List<T> list = new ArrayList<>();
        engine.iteration((key, value) -> {
            T t = Codec.decode(value, clazz);
            if (t != null) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                list.add(t);
            }
        });
        return (consumer != null) ? options.get(list, clazz) : list;
    }


    @Override
    public List<T> find(Predicate<T> predicate) {
        return find(predicate, null);
    }


    @Override
    public List<T> find(List<Long> ids) {
        List<T> list = new ArrayList<>();
        ids.forEach(id -> {
            byte[] key = Codec.encodeKey(id);
            byte[] value = engine.get(key);
            T t = (value != null) ? Codec.decode(value, clazz) : null;
            Optional.ofNullable(t).ifPresent(list::add);
        });
        return list;
    }


    @Override
    public List<T> find(long... ids) {
        List<T> list = new ArrayList<>();
        for (long id : ids) {
            byte[] key = Codec.encodeKey(id);
            byte[] value = engine.get(key);
            T t = (value != null) ? Codec.decode(value, clazz) : null;
            Optional.ofNullable(t).ifPresent(list::add);
        }
        return list;
    }


    @Override
    public List<T> findWithID(Predicate<Long> predicate, Consumer<FindOptions> consumer) {
        QFindOptions options = (consumer != null) ? new QFindOptions() : null;
        Optional.ofNullable(consumer).ifPresent(c -> c.accept(options));
        List<T> list = new ArrayList<>();
        engine.iteration((key, value) -> {
            long id = Codec.decodeKey(key);
            if (predicate.test(id)) {
                T t = Codec.decode(value, clazz);
                Optional.ofNullable(t).ifPresent(list::add);
            }
        });
        return (consumer != null) ? options.get(list, clazz) : list;
    }


    @Override
    public List<T> findWithID(Predicate<Long> predicate) {
        return findWithID(predicate, null);
    }


    @Override
    public List<T> findWithTime(Predicate<Long> predicate, Consumer<FindOptions> consumer) {
        return findWithID(id -> predicate.test(Plugin.toTimestamp(id)), consumer);
    }


    @Override
    public List<T> findWithTime(Predicate<Long> predicate) {
        return findWithTime(predicate, null);
    }


    @Override
    public T findFirst(Predicate<T> predicate) {
        AtomicReference<T> minT = new AtomicReference<>();
        engine.iteration((key, value) -> {
            long id = Codec.decodeKey(key);
            T t = Codec.decode(value, clazz);
            if (t != null && (minT.get() == null || id < minT.get().objectId())) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                minT.set(t);
            }
        });
        return minT.get();
    }


    @Override
    public T findFirst() {
        return findFirst(null);
    }


    @Override
    public T findLast(Predicate<T> predicate) {
        AtomicReference<T> maxT = new AtomicReference<>();
        engine.iteration((key, value) -> {
            long id = Codec.decodeKey(key);
            T t = Codec.decode(value, clazz);
            if (t != null && (maxT.get() == null || id > maxT.get().objectId())) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                maxT.set(t);
            }
        });
        return maxT.get();
    }


    @Override
    public T findLast() {
        return findLast(null);
    }


    @Override
    public T findOne(long id) {
        byte[] key = Codec.encodeKey(id);
        byte[] value = engine.get(key);
        return (value != null) ? Codec.decode(value, clazz) : null;
    }


    @Override
    public T findOne(Predicate<T> predicate) {
        return engine.iteration((key, value) -> {
            T t = Codec.decode(value, clazz);
            return t != null && predicate.test(t) ? t : null;
        });
    }


    @Override
    public T findWithIndex(Consumer<FindOptions> consumer) {
        QFindOptions options = new QFindOptions();
        consumer.accept(options);
        long id = indexer.getIndexId(clazz, options.indexName, options.indexValue);
        return findOne(id);
    }


    @Override
    public boolean exist(Consumer<FindOptions> consumer) {
        QFindOptions options = new QFindOptions();
        consumer.accept(options);
        return indexer.exist(clazz, options.indexName, options.indexValue);
    }


    @Override
    public void dropIndex(String fieldName) {
        List<T> list = findAll();
        indexer.dropIndex(list, fieldName);
    }


    @Override
    public long count(Predicate<T> predicate) {
        AtomicLong count = new AtomicLong(0);
        engine.iteration((key, value) -> {
            T t = Codec.decode(value, clazz);
            if (t != null) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                count.incrementAndGet();
            }
        });
        return count.get();
    }


    @Override
    public long count() {
        return count(null);
    }

}