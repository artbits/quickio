package com.github.artbits.quickio;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class QuickIO {

    public static void init() {
        DBHelper.init();
    }

    public static void save(IObject o) {
        Operator.save(o);
    }

    public static <T> void save(List<T> list) {
        Operator.save(list);
    }

    public static void update(IObject o, Consumer<Options> consumer) {
        Operator.update(o, consumer);
    }

    public static boolean delete(long id) {
        return Operator.delete(id);
    }

    public static <T> void delete(Class<T> tClass, Consumer<Options> consumer) {
        Operator.delete(tClass, consumer);
    }

    public static <T> T findFirst(Class<T> tClass) {
        return Operator.findFirst(tClass);
    }

    public static <T> T findLast(Class<T> tClass) {
        return Operator.findLast(tClass);
    }

    public static <T> T findOne(Class<T> tClass, Consumer<Options> consumer) {
        return Operator.findOne(tClass, consumer);
    }

    public static <T> List<T> find(Class<T> tClass) {
        return Operator.find(tClass);
    }

    public static <T> List<T> find(Class<T> tClass, Consumer<Options> consumer) {
        return Operator.find(tClass, consumer);
    }

    public static <T> List<T> find(Class<T> tClass, Predicate<T> predicate) {
        return Operator.find(tClass, predicate);
    }

    public static <T> List<T> find(Class<T> tClass, long... ids) {
        return Operator.find(tClass, ids);
    }

    public static <T> T find(Class<T> tClass, long id) {
        return Operator.find(tClass, id);
    }

}
