package com.github.artbits.quickio;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class Operator {

    static void save(IObject o) {
        o.id = (o.id() == 0 || Tools.getDigit(o.id()) < 18) ? Snowflake.nextId() : o.id();
        if (!DBHelper.put(Tools.asByteArray(o.id()),  Tools.asByteArray(o))) {
            o.id = 0;
        }
    }

    static <T> void save(List<T> list) {
        DBHelper.writeBatch(batch -> list.forEach(t -> {
            IObject o = (IObject) t;
            o.id = o.id() == 0 ? Snowflake.nextId() : o.id();
            batch.put(Tools.asByteArray(o.id()), Tools.asByteArray(o));
        }));
    }

    @SuppressWarnings("unchecked")
    static <T> void update(T t , Predicate<T> predicate) {
        Map<String, Object> tMap = new HashMap<>();
        for (Field field : t.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (field.getName().equals("id")) {
                    continue;
                }
                Object obj = field.get(t);
                if (obj != null) {
                    tMap.put(field.getName(), obj);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && Tools.equals(data.model, t.getClass().getSimpleName())) {
                T dbT = Tools.getObject(data);
                if (predicate.test(dbT)) {
                    long id = 0;
                    for (Map.Entry<String, Object> entry : tMap.entrySet()) {
                        try {
                            if (id == 0) {
                                id = Tools.getIDField(dbT.getClass()).getLong(dbT);
                            }
                            Field dbField = dbT.getClass().getDeclaredField(entry.getKey());
                            dbField.setAccessible(true);
                            dbField.set(dbT, entry.getValue());
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    DBHelper.put(Tools.asByteArray(id), Tools.asByteArray(dbT));
                }
            }
        });
    }

    static boolean delete(long id) {
        return DBHelper.delete(Tools.asByteArray(id));
    }

    static void delete(long... ids) {
        DBHelper.writeBatch(batch -> {
            for (long id : ids) {
                batch.delete(Tools.asByteArray(id));
            }
        });
    }

    static <T> void delete(Class<T> tClass) {
        DBHelper.writeBatch(batch -> DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && Tools.equals(data.model, tClass.getSimpleName())) {
                batch.delete(key);
            }
        }));
    }

    static <T> void delete(Class<T> tClass, Predicate<T> predicate) {
        DBHelper.writeBatch(batch -> DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && Tools.equals(data.model, tClass.getSimpleName())) {
                T t = Tools.getObject(data);
                if (predicate.test(t)) {
                    batch.delete(key);
                }
            }
        }));
    }

    static <T> T findFirst(Class<T> tClass) {
        final long[] minKey = {Long.MAX_VALUE};
        final byte[][] firstValue = new byte[1][];
        DBHelper.iteration((key, value) -> {
            long tKey = Tools.asLong(key);
            Data data = Tools.asData(value);
            if (data != null && Tools.equals(data.model, tClass.getSimpleName()) && (tKey < minKey[0])) {
                minKey[0] = tKey;
                firstValue[0] = value;
            }
        });
        return Tools.asObject(firstValue[0], tClass);
    }

    static <T> T findLast(Class<T> tClass) {
        final long[] maxKey = {Long.MIN_VALUE};
        final byte[][] lastValue = new byte[1][];
        DBHelper.iteration((key, value) -> {
            long tKey = Tools.asLong(key);
            Data data = Tools.asData(value);
            if (data != null && Tools.equals(data.model, tClass.getSimpleName()) && (tKey > maxKey[0])) {
                maxKey[0] = tKey;
                lastValue[0] = value;
            }
        });
        return Tools.asObject(lastValue[0], tClass);
    }

    static <T> T findOne(Class<T> tClass, Predicate<T> predicate) {
        return DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && Tools.equals(data.model, tClass.getSimpleName())) {
                T t = Tools.getObject(data);
                if (predicate.test(t)) {
                    return t;
                }
            }
            return null;
        });
    }

    static <T> List<T> find(Class<T> tClass) {
        List<T> list = new ArrayList<>();
        DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && Tools.equals(data.model, tClass.getSimpleName())) {
                T t = Tools.getObject(data);
                list.add(t);
            }
        });
        return list;
    }

    static <T> List<T> find(Class<T> tClass, Predicate<T> predicate) {
        List<T> list = new ArrayList<>();
        DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && Tools.equals(data.model, tClass.getSimpleName())) {
                T t = Tools.getObject(data);
                if (predicate.test(t)) {
                    list.add(t);
                }
            }
        });
        return list;
    }

    static <T> List<T> find(Class<T> tClass, Predicate<T> predicate, Consumer<Options> consumer) {
        Options options = new Options();
        consumer.accept(options);
        List<T> list = new ArrayList<>();
        List<T> finalList = list;
        DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && Tools.equals(data.model, tClass.getSimpleName())) {
                T t = Tools.getObject(data);
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

    static <T> List<T> find(Class<T> tClass, long... ids) {
        List<T> list = new ArrayList<>();
        for (long id : ids) {
            byte[] key = Tools.asByteArray(id);
            byte[] value = DBHelper.get(key);
            T t = (value != null) ? Tools.asObject(value, tClass) : null;
            list.add(t);
        }
        return list;
    }

    static <T> T find(Class<T> tClass, long id) {
        byte[] key = Tools.asByteArray(id);
        byte[] value = DBHelper.get(key);
        return (value != null) ? Tools.asObject(value, tClass) : null;
    }

}
