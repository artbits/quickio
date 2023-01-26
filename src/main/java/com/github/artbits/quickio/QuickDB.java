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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.artbits.quickio.Tools.*;

class QuickDB extends LevelIO {

    private final QuickIO.Options options;
    private final Indexer indexer;


    QuickDB(Consumer<QuickIO.Options> consumer) {
        options = new QuickIO.Options();
        consumer.accept(options);
        if (options.basePath == null) {
            options.outBasePath = Constants.OUT_DB_PATH;
            options.basePath = Constants.DB_PATH;
        } else {
            options.outBasePath = (options.basePath + "/" + Constants.OUT_DB_PATH).replaceAll("//", "/");
            options.basePath = (options.basePath + "/" + Constants.DB_PATH).replaceAll("//", "/");
        }
        options.shareable = Optional.ofNullable(options.shareable).orElse(true);
        open(options);
        indexer = new Indexer(options.basePath, options.name);
        closeListener(() -> Optional.ofNullable(indexer).ifPresent(LevelIO::close));
    }


    QuickDB(String name) {
        this(options -> options.name = name);
    }


    public <T extends QuickIO.Object> void save(T t) {
        t.id = (t.id() == 0 || getDigit(t.id()) < 18) ? QuickIO.id() : t.id();
        indexer.setIndex(t);
        put(asBytes(t.id), asBytes(t), e -> {
            indexer.removeIndex(t);
            t.id = 0L;
        });
    }


    public <T extends QuickIO.Object> void save(List<T> list) {
        list.forEach(t -> t.id = (t.id() == 0) ? QuickIO.id() : t.id());
        indexer.setIndexes(list);
        writeBatch(batch -> list.forEach(t -> batch.put(asBytes(t.id), asBytes(t))), e -> {
            indexer.removeIndexList(list);
            list.forEach(t -> t.id = 0L);
        });
    }


    @SuppressWarnings("unchecked")
    public <T extends QuickIO.Object> void update(T t , Predicate<T> predicate) {
        List<T> newLocalTList = new ArrayList<>();
        List<T> oldLocalTList = new ArrayList<>();
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
                        oldLocalTList.add((T) localT.clone());
                        Tools.setFieldValue(localT, localField, tFieldValue);
                        newLocalTList.add(localT);
                    }
                });
            }
        });
        indexer.setIndexes(newLocalTList);
        writeBatch(batch -> newLocalTList.forEach(t1 -> batch.put(asBytes(t1.id()), asBytes(t1))), e -> {
            indexer.removeIndexList(newLocalTList);
            indexer.setIndexes(oldLocalTList);
        });
    }


    public boolean delete(long id) {
        delete(asBytes(id));
        indexer.removeIndex(id);
        return true;
    }


    public void delete(long... ids) {
        writeBatch(batch -> {
            for (long id : ids) {
                batch.delete(asBytes(id));
            }
        });
        indexer.removeIndexes(ids);
    }


    public void delete(List<Long> ids) {
        writeBatch(batch -> ids.forEach(id -> batch.delete(asBytes(id.longValue()))));
        indexer.removeIndexes(ids);
    }


    public <T extends QuickIO.Object> void delete(Class<T> tClass, Predicate<T> predicate) {
        List<Long> ids = new ArrayList<>();
        writeBatch(batch -> iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                batch.delete(key);
                ids.add(t.id());
            }
        }));
        indexer.removeIndexes(ids);
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


    public <T extends QuickIO.Object> List<T> find(Class<T> tClass, List<Long> ids) {
        List<T> list = new ArrayList<>();
        ids.forEach(id -> {
            byte[] key = asBytes(id.longValue());
            byte[] value = get(key);
            T t = (value != null) ? asObject(value, tClass) : null;
            Optional.ofNullable(t).ifPresent(list::add);
        });
        return list;
    }


    public <T extends QuickIO.Object> List<T> find(Class<T> tClass, long... ids) {
        List<T> list = new ArrayList<>();
        for (long id : ids) {
            byte[] key = asBytes(id);
            byte[] value = get(key);
            T t = (value != null) ? asObject(value, tClass) : null;
            Optional.ofNullable(t).ifPresent(list::add);
        }
        return list;
    }


    public <T extends QuickIO.Object> T find(Class<T> tClass, long id) {
        byte[] key = asBytes(id);
        byte[] value = get(key);
        return (value != null) ? asObject(value, tClass) : null;
    }


    public <T extends QuickIO.Object> List<T> findWithID(Class<T> tClass, Predicate<Long> predicate, Consumer<FindOptions> consumer) {
        FindOptions<T> options = (consumer != null) ? new FindOptions<>(tClass) : null;
        Optional.ofNullable(consumer).ifPresent(c -> c.accept(options));
        List<T> list = new ArrayList<>();
        iteration((key, value) -> {
            long id = Tools.asLong(key);
            if (predicate.test(id)) {
                T t = asObject(value, tClass);
                Optional.ofNullable(t).ifPresent(list::add);
            }
        });
        return (consumer != null) ? options.get(list) : list;
    }


    public <T extends QuickIO.Object> List<T> findWithID(Class<T> tClass, Predicate<Long> predicate) {
        return findWithID(tClass, predicate, null);
    }


    public <T extends QuickIO.Object> List<T> findWithTime(Class<T> tClass, Predicate<Long> predicate, Consumer<FindOptions> consumer) {
        return findWithID(tClass, id -> predicate.test(QuickIO.toTimestamp(id)), consumer);
    }


    public <T extends QuickIO.Object> List<T> findWithTime(Class<T> tClass, Predicate<Long> predicate) {
        return findWithTime(tClass, predicate, null);
    }


    public <T extends QuickIO.Object> T findWithIndex(Class<T> tClass, Consumer<FindOptions> consumer) {
        FindOptions<T> options = new FindOptions<>(tClass);
        consumer.accept(options);
        long id = indexer.getIndexId(tClass, options.indexName, options.indexValue);
        return find(tClass, id);
    }


    public <T extends QuickIO.Object> boolean exist(Class<T> tClass, Consumer<FindOptions> consumer) {
        FindOptions<T> options = new FindOptions<>(tClass);
        consumer.accept(options);
        return indexer.exist(tClass, options.indexName, options.indexValue);
    }


    public <T extends QuickIO.Object> void dropIndex(Class<T> tClass, String fieldName) {
        List<T> list = find(tClass);
        indexer.dropIndex(list, fieldName);
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


    public void export(Consumer<String> consumer1, Consumer<Exception> consumer2) {
        String basePath = String.format("%s%s/", options.outBasePath, options.name);
        String fileName = String.format("%s_%d.txt", options.name, System.currentTimeMillis());
        Exporter exporter = new Exporter(basePath, fileName);
        StringBuilder builder = new StringBuilder();
        iteration((key, value) -> {
            Object object = asObject(value);
            if (object != null) {
                String className = object.getClass().getSimpleName();
                Exporter.DBObject dbObject = new Exporter.DBObject(className, object);
                builder.append(new JSONObject(dbObject)).append("\n");
            }
        });
        exporter.exportFile(builder.toString(), consumer1, consumer2);
    }


    public static void open(String name, Consumer<QuickIO.DB> consumer1, Consumer<Exception> consumer2) {
        try (QuickIO.DB db = new QuickIO.DB(name)) {
            consumer1.accept(db);
        } catch (Exception e) {
            Optional.ofNullable(consumer2)
                    .orElseThrow(() -> new RuntimeException(e))
                    .accept(e);
        }
    }


    public static void open(String name, Consumer<QuickIO.DB> consumer) {
        open(name, consumer,null);
    }


    public static <T> T openGet(String name, Function<QuickIO.DB, T> function, Consumer<Exception> consumer) {
        try (QuickIO.DB db = new QuickIO.DB(name)) {
            return function.apply(db);
        } catch (Exception e) {
            Optional.ofNullable(consumer)
                    .orElseThrow(() -> new RuntimeException(e))
                    .accept(e);
            return null;
        }
    }


    public static <T> T openGet(String name, Function<QuickIO.DB, T> function) {
        return openGet(name, function, null);
    }

}