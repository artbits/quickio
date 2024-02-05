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

import com.github.artbits.quickio.exception.QIOException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

final class ReflectObject<T extends IOEntity> {

    private final Map<String, Field> fieldMap = new HashMap<>();
    private final T t;
    private long id;
    private long createdAt;


    ReflectObject(T t) {
        this.t = t;
        Class<?> clazz = t.getClass();
        while (clazz != null){
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if ("_id".equals(field.getName())) {
                    id = t.objectId();
                    continue;
                }
                if ("createdAt".equals(field.getName())) {
                    createdAt = t.createdAt();
                    continue;
                }
                fieldMap.put(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }
    }


    void traverseFields(BiConsumer<String, Object> consumer) {
        try {
            for (Field field : fieldMap.values()) {
                consumer.accept(field.getName(), field.get(t));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    void traverseAnnotationFields(Class<? extends Annotation> annotationClass, BiConsumer<String, Object> consumer) {
        try {
            for (Field field : fieldMap.values()) {
                if (field.isAnnotationPresent(annotationClass)) {
                    consumer.accept(field.getName(), field.get(t));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    boolean contains(String fieldName) {
        return fieldMap.containsKey(fieldName);
    }


    boolean containsAnnotation(Class<? extends Annotation> annotationClass) {
        for (Field field : fieldMap.values()) {
            if (field.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }
        return false;
    }


    Object getValue(String fieldName) {
        try {
            Field field = fieldMap.getOrDefault(fieldName, null);
            return (field != null) ? field.get(t) : null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    void setValue(String fieldName, Object value) {
        try {
            Field field = fieldMap.getOrDefault(fieldName, null);
            if (field != null) {
                field.set(t, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    Class<?> getType(String fieldName) {
        Field field = fieldMap.getOrDefault(fieldName, null);
        return field.getType();
    }


    BigDecimal getBigDecimalValue(String fieldName) {
        try {
            switch (fieldName) {
                case "_id": return BigDecimal.valueOf(id);
                case "createdAt": return BigDecimal.valueOf(createdAt);
            }
            Field field = fieldMap.getOrDefault(fieldName, null);
            Optional.ofNullable(field).orElseThrow(() -> new QIOException(Constants.FIELD_DOES_NOT_EXIST));
            switch (field.getType().getSimpleName().toLowerCase()) {
                case "int":
                case "integer": return BigDecimal.valueOf((Integer) field.get(t));
                case "byte": return BigDecimal.valueOf((Byte) field.get(t));
                case "short": return BigDecimal.valueOf((Short) field.get(t));
                case "long": return BigDecimal.valueOf((Long) field.get(t));
                case "float": return BigDecimal.valueOf((Float) field.get(t));
                case "double": return BigDecimal.valueOf((Double) field.get(t));
                default: throw new QIOException(Constants.FIELD_NOT_NUMERICAL_TYPE);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    T get() {
        return t;
    }

}