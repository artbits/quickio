package com.github.artbits.quickio;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class Options {

    private String sortField;
    private long sort;
    private long limit;


    Options() { }


    public void sort(String field, int sort) {
        this.sortField = field;
        this.sort = sort;
    }


    public void limit(long limit) {
        this.limit = limit;
    }


    <T> List<T> sortList(List<T> list) {
        if (sortField == null || sort < -1 || sort == 0 || sort > 1) {
            return list;
        }
        Comparator<T> comparing = Comparator.comparing(t -> {
            try {
                Field field = Tools.getFields(t.getClass()).get(sortField);
                return Double.parseDouble(String.valueOf(field.get(t)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        comparing = (sort == 1) ? comparing : comparing.reversed();
        return list.stream().sorted(comparing).collect(Collectors.toList());
    }


    <T> List<T> limitList(List<T> list) {
        if (limit <= 0) {
            return list;
        }
        return list.stream().limit(limit).collect(Collectors.toList());
    }

}