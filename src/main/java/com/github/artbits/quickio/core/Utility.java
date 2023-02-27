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

import com.github.artbits.quickio.annotations.Index;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

final class Utility {

    static int getDigit(long i){
        i = i > 0 ? i : -i;
        return i == 0 ? 1 : (int) Math.log10(i) + 1;
    }


    static Map<String, Field> getFields(Class<?> clazz) {
        Map<String, Field> map = new HashMap<>();
        while (clazz != null){
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                map.put(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }
        return map;
    }


    static List<Field> getAnnotationFields(Object o) {
        Map<String, Field> map = getFields(o.getClass());
        return new ArrayList<>(map.values()).stream()
                .filter(field -> field.isAnnotationPresent(Index.class))
                .collect(Collectors.toList());
    }


    static Object getFieldValue(Object object, Field field) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    static void setFieldValue(Object object, Field field, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    static String toDateTime(long timestamp) {
        return Instant
                .ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}