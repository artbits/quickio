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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
        sortFieldName = Optional.ofNullable((fieldName == null || fieldName.isEmpty()) ? null : fieldName)
                .orElseThrow(() -> new RuntimeException("The first parameter cannot be null or empty"));
        sortValue = Optional.ofNullable((value < -1 || value > 1) ? null : value)
                .orElseThrow(() -> new RuntimeException("The second parameter can only be 1 or -1"));
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
        Stream<T> stream = (list == null || list.isEmpty()) ? null : list.stream();
        if (stream == null) {
            return list;
        }
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


    private <K> Comparator<K> createComparator() {
        Field sortField = Tools.getFields(tClass).getOrDefault(sortFieldName, null);
        Optional.ofNullable(sortField).orElseThrow(() -> new RuntimeException("This field does not exist"));
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