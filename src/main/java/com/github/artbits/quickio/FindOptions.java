package com.github.artbits.quickio;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FindOptions<T> {

    private final Class<T> tClass;
    private String sortFieldName;
    private long sortValue;
    private long skipSize;
    private long limitSize;


    FindOptions(Class<T> tClass) {
        this.tClass = tClass;
    }


    public FindOptions sort(String fieldName, int value) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new RuntimeException("The first parameter of sort method cannot be null or empty");
        }
        if (value < -1 || value > 1) {
            throw new RuntimeException("The second parameter of the sort method can only be 1 or -1");
        }
        sortFieldName = fieldName;
        sortValue = value;
        return this;
    }


    public FindOptions skip(long size) {
        skipSize = size;
        return this;
    }


    public FindOptions limit(long size) {
        limitSize = size;
        return this;
    }


    List<T> get(List<T> list) {
        if (list.isEmpty()) {
            return list;
        }
        Stream<T> stream = list.stream();
        if (sortValue != 0) {
            Comparator<T> comparator = createComparator();
            comparator = (sortValue == 1) ? comparator : comparator.reversed();
            stream = stream.parallel().sorted(comparator);
            stream = stream.sequential();
        }
        if (skipSize > 0) {
            stream = stream.skip(skipSize);
        }
        if (limitSize > 0) {
            stream = stream.limit(limitSize);
        }
        return stream.collect(Collectors.toList());
    }


    <K> Comparator<K> createComparator() {
        Field sortField = Tools.getFields(tClass).getOrDefault(sortFieldName, null);
        if (sortField == null) {
            throw new RuntimeException("This field does not exist");
        }
        switch (sortField.getType().getSimpleName().toLowerCase()) {
            case "byte":
            case "short":
            case "int":
            case "integer":
                return Comparator.comparingInt(t -> {
                    Field field = Tools.getFields(t.getClass()).get(sortFieldName);
                    Object fieldValue = Tools.getFieldValue(t, field);
                    return (int) fieldValue;
                });
            case "long":
                return Comparator.comparingLong(t -> {
                    Field field = Tools.getFields(t.getClass()).get(sortFieldName);
                    Object fieldValue = Tools.getFieldValue(t, field);
                    return (long) fieldValue;
                });
            case "float":
            case "double":
                return Comparator.comparingDouble(t -> {
                    Field field = Tools.getFields(t.getClass()).get(sortFieldName);
                    Object fieldValue = Tools.getFieldValue(t, field);
                    return (double) fieldValue;
                });
            default:
                throw new RuntimeException("This field does not support sorting");
        }
    }

}