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

package com.github.artbits.quickio.core;

import com.github.artbits.quickio.api.FindOptions;
import com.github.artbits.quickio.exception.QIOException;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class QFindOptions implements FindOptions {

    private String sortFieldName;
    private long sortValue;
    private long skipSize;
    private long limitSize;

    String indexName;
    Object indexValue;


    @Override
    public FindOptions sort(String fieldName, int value) {
        if (fieldName == null || fieldName.isEmpty()) {
            throw new QIOException(Constants.SORTING_FIELD_NAME_ILLEGAL);
        }
        if (value < -1 || value > 1) {
            throw new QIOException(Constants.SORTING_PARAMETER_VALUE_ILLEGAL);
        }
        sortFieldName = fieldName;
        sortValue = value;
        return this;
    }


    @Override
    public FindOptions skip(long size) {
        skipSize = size;
        return this;
    }


    @Override
    public FindOptions limit(long size) {
        limitSize = size;
        return this;
    }


    @Override
    public void index(String fieldName, Object fieldValue) {
        indexName = Optional.ofNullable(fieldName).orElseThrow(NullPointerException::new);
        indexValue = Optional.ofNullable(fieldValue).orElseThrow(NullPointerException::new);
    }


    <T> List<T> get(List<T> list, Class<T> clazz) {
        Stream<T> stream = (list == null || list.isEmpty()) ? null : list.stream();
        if (stream == null) {
            return list;
        }
        if (sortValue != 0) {
            Comparator<T> comparator = createComparator(clazz);
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


    private <K, T> Comparator<K> createComparator(Class<T> clazz) {
        Field sortField = Utility.getFields(clazz).getOrDefault(sortFieldName, null);
        Optional.ofNullable(sortField).orElseThrow(() -> new QIOException(Constants.FIELD_DOES_NOT_EXIST));
        switch (sortField.getType().getSimpleName().toLowerCase()) {
            case "byte":
            case "short":
            case "int":
            case "integer":
                return Comparator.comparingInt(t -> {
                    Field field = Utility.getFields(t.getClass()).get(sortFieldName);
                    Object fieldValue = Utility.getFieldValue(t, field);
                    return (int) fieldValue;
                });
            case "long":
                return Comparator.comparingLong(t -> {
                    Field field = Utility.getFields(t.getClass()).get(sortFieldName);
                    Object fieldValue = Utility.getFieldValue(t, field);
                    return (long) fieldValue;
                });
            case "float":
            case "double":
                return Comparator.comparingDouble(t -> {
                    Field field = Utility.getFields(t.getClass()).get(sortFieldName);
                    Object fieldValue = Utility.getFieldValue(t, field);
                    return (double) fieldValue;
                });
            default:
                throw new QIOException(Constants.FIELD_DOES_NOT_SUPPORT_SORTING);
        }
    }

}