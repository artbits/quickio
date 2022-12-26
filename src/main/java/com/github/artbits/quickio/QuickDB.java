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


    public <T extends QuickIO.Object> void delete(Class<T> tClass) {
        writeBatch(batch -> iteration((key, value) -> {
            if (asObject(value, tClass) != null) {
                batch.delete(key);
            }
        }));
    }


    public <T extends QuickIO.Object> void delete(Class<T> tClass, Predicate<T> predicate) {
        writeBatch(batch -> iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null && predicate.test(t)) {
                batch.delete(key);
            }
        }));
    }


    public <T extends QuickIO.Object> T findFirst(Class<T> tClass, Predicate<T> predicate) {
        final long[] minKey = {Long.MAX_VALUE};
        AtomicReference<T> minT = new AtomicReference<>();
        iteration((key, value) -> {
            long tKey = Tools.asLong(key);
            T t = asObject(value, tClass);
            if (t != null && tKey < minKey[0]) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                minKey[0] = tKey;
                minT.set(t);
            }
        });
        return minT.get();
    }


    public <T extends QuickIO.Object> T findFirst(Class<T> tClass) {
        return findFirst(tClass, null);
    }


    public <T extends QuickIO.Object> T findLast(Class<T> tClass, Predicate<T> predicate) {
        final long[] maxKey = {Long.MIN_VALUE};
        AtomicReference<T> maxT = new AtomicReference<>();
        iteration((key, value) -> {
            long tKey = Tools.asLong(key);
            T t = asObject(value, tClass);
            if (t != null && tKey > maxKey[0]) {
                if (predicate != null && !predicate.test(t)) {
                    return;
                }
                maxKey[0] = tKey;
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


    public <T extends QuickIO.Object> List<T> find(Class<T> tClass) {
        List<T> list = new ArrayList<>();
        iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null) {
                list.add(t);
            }
        });
        return list;
    }


    public <T extends QuickIO.Object> List<T> find(Class<T> tClass, Predicate<T> predicate) {
        List<T> list = new ArrayList<>();
        iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null && predicate.test(t)) {
                list.add(t);
            }
        });
        return list;
    }


    public <T extends QuickIO.Object> List<T> find(Class<T> tClass, Predicate<T> predicate, Consumer<FindOptions> consumer) {
        FindOptions<T> options = new FindOptions<>(tClass);
        consumer.accept(options);
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
        return options.get(list);
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