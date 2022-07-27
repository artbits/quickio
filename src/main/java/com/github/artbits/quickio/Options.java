package com.github.artbits.quickio;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Options extends Condition {

    private String sortField;
    private long sort;
    private long limit;

    private Options() {
        super();
    }

    static Options getInstance() {
        return new Options();
    }

    public void sort(String field, int sort) {
        this.sortField = field;
        this.sort = sort;
    }

    public void limit(long limit) {
        this.limit = limit;
    }

    <T> List<T> sortList(List<T> list) {
        if (sortField == null || sort < -1 || sort == 0 || sort > 1) return list;
        Comparator<T> comparing = Comparator.comparing(t -> {
            try {
                Field field = Tools.getFields(t.getClass()).get(sortField);
                field.setAccessible(true);
                if ("String".equals(field.getType().getSimpleName())) {
                    return Double.parseDouble(String.valueOf(field.get(t)));
                }
                return field.getDouble(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        comparing = (sort == 1) ? comparing : comparing.reversed();
        return list.stream().sorted(comparing).collect(Collectors.toList());
    }

    <T> List<T> limitList(List<T> list) {
        if (limit <= 0) return list;
        return list.stream().limit(limit).collect(Collectors.toList());
    }

    <T> boolean filter(T t) {
        Map<String, Field> fieldMap = Tools.getFields(t.getClass());
        for (Map.Entry<String, String[]> entry : super.getMap().entrySet()) {
            try {
                String[] objects = entry.getValue();
                Field field = fieldMap.get(entry.getKey());
                field.setAccessible(true);
                if ("eq".equals(objects[1]) && !String.valueOf(field.get(t)).equals(objects[0])) {
                    return false;
                }
                if ("ne".equals(objects[1]) && String.valueOf(field.get(t)).equals(objects[0])) {
                    return false;
                }
                if ("lt".equals(objects[1]) && Double.parseDouble(String.valueOf(field.get(t))) >= Double.parseDouble(objects[0])) {
                    return false;
                }
                if ("gt".equals(objects[1]) && Double.parseDouble(String.valueOf(field.get(t))) <= Double.parseDouble(objects[0])) {
                    return false;
                }
                if ("lte".equals(objects[1]) && Double.parseDouble(String.valueOf(field.get(t))) > Double.parseDouble(objects[0])) {
                    return false;
                }
                if ("gte".equals(objects[1]) && Double.parseDouble(String.valueOf(field.get(t))) < Double.parseDouble(objects[0])) {
                    return false;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

}
