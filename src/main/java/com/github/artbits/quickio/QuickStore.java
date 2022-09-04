package com.github.artbits.quickio;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.artbits.quickio.Tools.asBytes;
import static com.github.artbits.quickio.Tools.asObject;

class QuickStore extends IO {

    QuickStore(String path) {
        super("data/store/" + path);
    }


    public void save(IObject o) {
        if (o.id() == 0 || Tools.getDigit(o.id()) < 18) {
            o.id = Snowflake.nextId();
        }
        boolean bool = put(asBytes(o.id()),  asBytes(o));
        if (!bool) {
            o.id = 0L;
        }
    }


    public <T> void save(List<T> list) {
        writeBatch(batch -> list.forEach(t -> {
            IObject o = (IObject) t;
            o.id = o.id() == 0 ? Snowflake.nextId() : o.id();
            batch.put(asBytes(o.id()), asBytes(o));
        }));
    }


    @SuppressWarnings("unchecked")
    public <T> void update(T t , Predicate<T> predicate) {
        Map<String, Field> tMap = Tools.getFields(t.getClass());
        iteration((key, value) -> {
            T localT = (T) asObject(value, t.getClass());
            if (localT != null && predicate.test(localT)) {
                Map<String, Field> localTMap = Tools.getFields(localT.getClass());
                long id = 0L;
                for (Map.Entry<String, Field> tEntry : tMap.entrySet()) {
                    try {
                        if (Objects.equals(tEntry.getKey(), "id")) {
                            id = localTMap.get("id").getLong(localT);
                            continue;
                        }
                        Object tObject = tEntry.getValue().get(t);
                        if (tObject == null) {
                            continue;
                        }
                        Field localField = localTMap.getOrDefault(tEntry.getKey(), null);
                        if (localField != null) {
                            localField.set(localT, tObject);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                put(asBytes(id), asBytes(localT));
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


    public <T> void delete(Class<T> tClass) {
        writeBatch(batch -> iteration((key, value) -> {
            if (asObject(value, tClass) != null) {
                batch.delete(key);
            }
        }));
    }


    public <T> void delete(Class<T> tClass, Predicate<T> predicate) {
        writeBatch(batch -> iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null && predicate.test(t)) {
                batch.delete(key);
            }
        }));
    }


    public <T> T findFirst(Class<T> tClass) {
        final long[] minKey = {Long.MAX_VALUE};
        AtomicReference<T> minT = new AtomicReference<>();
        iteration((key, value) -> {
            long tKey = Tools.asLong(key);
            T t = asObject(value, tClass);
            if (t != null && tKey < minKey[0]) {
                minKey[0] = tKey;
                minT.set(t);
            }
        });
        return minT.get();
    }


    public <T> T findLast(Class<T> tClass) {
        final long[] maxKey = {Long.MIN_VALUE};
        AtomicReference<T> maxT = new AtomicReference<>();
        iteration((key, value) -> {
            long tKey = Tools.asLong(key);
            T t = asObject(value, tClass);
            if (t != null && tKey > maxKey[0]) {
                maxKey[0] = tKey;
                maxT.set(t);
            }
        });
        return maxT.get();
    }


    public <T> T findOne(Class<T> tClass, Predicate<T> predicate) {
        return iteration((key, value) -> {
            T t = asObject(value, tClass);
            return t != null && predicate.test(t) ? t : null;
        });
    }


    public <T> List<T> find(Class<T> tClass) {
        List<T> list = new ArrayList<>();
        iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null) {
                list.add(t);
            }
        });
        return list;
    }


    public <T> List<T> find(Class<T> tClass, Predicate<T> predicate) {
        List<T> list = new ArrayList<>();
        iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null && predicate.test(t)) {
                list.add(t);
            }
        });
        return list;
    }


    public <T> List<T> find(Class<T> tClass, Predicate<T> predicate, Consumer<Options> consumer) {
        Options options = new Options();
        consumer.accept(options);
        List<T> list = new ArrayList<>();
        List<T> finalList = list;
        iteration((key, value) -> {
            T t = asObject(value, tClass);
            if (t != null) {
                if (predicate == null) {
                    finalList.add(t);
                } else if (predicate.test(t)) {
                    finalList.add(t);
                }
            }
        });
        list = options.sortList(list);
        list = options.limitList(list);
        return list;
    }


    public <T> List<T> find(Class<T> tClass, long... ids) {
        List<T> list = new ArrayList<>();
        for (long id : ids) {
            byte[] key = asBytes(id);
            byte[] value = get(key);
            T t = (value != null) ? asObject(value, tClass) : null;
            list.add(t);
        }
        return list;
    }


    public <T> T find(Class<T> tClass, long id) {
        byte[] key = asBytes(id);
        byte[] value = get(key);
        return (value != null) ? asObject(value, tClass) : null;
    }

}
