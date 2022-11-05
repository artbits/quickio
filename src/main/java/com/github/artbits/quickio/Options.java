package com.github.artbits.quickio;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class Options {

    private String sortFieldName;
    private long sort;
    private long limit;


    Options() { }


    public void sort(String fieldName, int sort) {
        this.sortFieldName = fieldName;
        this.sort = sort;
    }


    public void limit(long limit) {
        this.limit = limit;
    }


    <T> List<T> limitList(List<T> list) {
        return (limit <= 0) ? list : list.stream().limit(limit).collect(Collectors.toList());
    }


    <T> List<T> sortList(List<T> list, Class<T> tClass) {
        if (list.isEmpty() || sortFieldName == null || sort < -1 || sort == 0 || sort > 1) {
            return list;
        }
        Field sortField = Tools.getFields(tClass).getOrDefault(sortFieldName, null);
        if (sortField == null) {
            throw new RuntimeException("This field does not exist");
        }
        Comparator<T> comparing;
        switch (sortField.getType().getSimpleName().toLowerCase()) {
            case "byte":
            case "short":
            case "int":
            case "integer":
                comparing = Comparator.comparingInt(t -> {
                    Field field = Tools.getFields(t.getClass()).get(sortFieldName);
                    Object fieldValue = Tools.getFieldValue(t, field);
                    return (int) fieldValue;
                });
                break;
            case "long":
                comparing = Comparator.comparingLong(t -> {
                    Field field = Tools.getFields(t.getClass()).get(sortFieldName);
                    Object fieldValue = Tools.getFieldValue(t, field);
                    return (long) fieldValue;
                });
                break;
            case "float":
            case "double":
                comparing = Comparator.comparingDouble(t -> {
                    Field field = Tools.getFields(t.getClass()).get(sortFieldName);
                    Object fieldValue = Tools.getFieldValue(t, field);
                    return (double) fieldValue;
                });
                break;
            default:
                throw new RuntimeException("This field does not support sorting");
        }
        comparing = (sort == 1) ? comparing : comparing.reversed();
        return list.stream().sorted(comparing).collect(Collectors.toList());
    }

}