package com.github.artbits.quickio;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class Operator {


    static void save(IObject o) {
        o.setId((o.id() == 0 || Tools.getDigit(o.id()) < 18) ? Snowflake.nextId() : o.id());
        if (!DBHelper.put(Tools.asByteArray(o.id()),  Tools.asByteArray(o))) {
            o.setId(0);
        }
    }


    static <T> void save(List<T> list) {
        DBHelper.writeBatch(batch -> list.forEach(t -> {
            IObject o = (IObject) t;
            o.setId(o.id() == 0 ? Snowflake.nextId() : o.id());
            batch.put(Tools.asByteArray(o.id()), Tools.asByteArray(o));
        }));
    }


    static void update(IObject o, Consumer<Options> consumer) {
        Options options = Options.getInstance();
        consumer.accept(options);
        DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && data.getModel().equals(o.getClass().getName())) {
                IObject dbObject = Tools.asObject(value, o.getClass());
                if (options.filter(dbObject)) {
                    try {
                        for (Field field : o.getClass().getDeclaredFields()) {
                            if (field.get(o) != null) {
                                Field dbField = dbObject.getClass().getDeclaredField(field.getName());
                                dbField.setAccessible(true);
                                dbField.set(dbObject, field.get(o));
                            }
                        }
                        DBHelper.put(Tools.asByteArray(dbObject.id()), Tools.asByteArray(dbObject));
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }


    static boolean delete(long id) {
        return DBHelper.delete(Tools.asByteArray(id));
    }


    static <T> void delete(Class<T> tClass, Consumer<Options> consumer) {
        Options options = Options.getInstance();
        consumer.accept(options);
        DBHelper.writeBatch(batch -> DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && data.getModel().equals(tClass.getName())) {
                T t = Tools.asObject(value, tClass);
                if (options.filter(t)) {
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
            if (data != null && data.getModel().equals(tClass.getName()) && (tKey < minKey[0])) {
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
            if (data != null && data.getModel().equals(tClass.getName()) && (tKey > maxKey[0])) {
                maxKey[0] = tKey;
                lastValue[0] = value;
            }
        });
        return Tools.asObject(lastValue[0], tClass);
    }


    static <T> T findOne(Class<T> tClass, Consumer<Options> consumer) {
        Options options = Options.getInstance();
        consumer.accept(options);
        return DBHelper.iteration((BiFunction<byte[], byte[], T>) (key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && data.getModel().equals(tClass.getName())) {
                T t = Tools.asObject(value, tClass);
                if (options.filter(t)) {
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
            if (data != null && data.getModel().equals(tClass.getName())) {
                list.add(Tools.asObject(value, tClass));
            }
        });
        return list;
    }


    static <T> List<T> find(Class<T> tClass, Consumer<Options> consumer) {
        Options options = Options.getInstance();
        consumer.accept(options);
        List<T> list = new ArrayList<>();
        List<T> finalList = list;
        DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && data.getModel().equals(tClass.getName())) {
                T t = Tools.asObject(value, tClass);
                if (options.filter(t)) {
                    finalList.add(t);
                }
            }
        });
        list = options.sortList(list);
        list = options.limitList(list);
        return list;
    }


    static <T> List<T> find(Class<T> tClass, Predicate<T> predicate) {
        List<T> list = new ArrayList<>();
        DBHelper.iteration((key, value) -> {
            Data data = Tools.asData(value);
            if (data != null && data.getModel().equals(tClass.getName())) {
                T t = Tools.asObject(value, tClass);
                if (predicate.test(t)) {
                    list.add(t);
                }
            }
        });
        return list;
    }


    static <T> List<T> find(Class<T> tClass, long... ids) {
        List<T> list = new ArrayList<>();
        for (long id : ids) {
            byte[] key = Tools.asByteArray(id);
            byte[] value = DBHelper.get(key);
            list.add((value != null) ? Tools.asObject(value, tClass) : null);
        }
        return list;
    }


    static <T> T find(Class<T> tClass, long id) {
        byte[] key = Tools.asByteArray(id);
        byte[] value = DBHelper.get(key);
        return (value != null) ? Tools.asObject(value, tClass) : null;
    }


}
