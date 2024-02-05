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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

final class JsonObject {

    private final Map<String, String> map = new LinkedHashMap<>();


    <T> JsonObject(T t) {
        beanToMap(t);
    }


    private <T> void beanToMap(T t) {
        try {
            map.put("\"_id\"", null);
            Class<?> clazz = t.getClass();
            while (clazz != null) {
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    putMap(field.getName(), field.get(t));
                }
                clazz = clazz.getSuperclass();
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private void putMap(String fieldName, Object fieldValue) {
        if (fieldName == null || fieldValue == null || fieldValue instanceof Enum<?>) {
            return;
        } else {
            fieldName = "\"" + fieldName + "\"";
        }

        if (fieldValue instanceof String || fieldValue instanceof Character) {
            if (!"\u0000".equals(String.valueOf(fieldValue))) {
                map.put(fieldName, "\"" + fieldValue + "\"");
            }
            return;
        }
        if (fieldValue instanceof Byte || fieldValue instanceof Short
                || fieldValue instanceof Integer || fieldValue instanceof Long
                || fieldValue instanceof Boolean || fieldValue instanceof Float
                || fieldValue instanceof Double || fieldValue instanceof BigInteger
                || fieldValue instanceof BigDecimal) {
            map.put(fieldName, String.valueOf(fieldValue));
            return;
        }
        if (fieldValue.getClass().isArray()) {
            map.put(fieldName, arrayToJSONString(fieldValue));
            return;
        }
        if (fieldValue instanceof Collection<?>) {
            map.put(fieldName, collectionToJSONString((Collection<?>) fieldValue));
            return;
        }
        if (fieldValue instanceof Map<?, ?>) {
            map.put(fieldName, mapToJSONString((Map<?, ?>) fieldValue));
            return;
        }
        map.put(fieldName, new JsonObject(fieldValue).toString());
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append("{");
        map.forEach((k, v) -> Optional.ofNullable(v).ifPresent(s -> builder.append(k).append(":").append(v).append(",")));
        return ((builder.length() > 2) ? builder.deleteCharAt(builder.length() - 1) : builder).append("}").toString();
    }


    private static String charArrayToJSONString(char[] chars) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (char c : chars) {
            builder.append("\"").append(c).append("\"").append(",");
        }
        return ((builder.length() > 2) ? builder.deleteCharAt(builder.length() - 1) : builder).append("]").toString();
    }


    private static <T> String objectArrayToJSONString(T[] arrays) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Object o : arrays) {
            ifTextType(o, () -> {
                builder.append("\"").append(o).append("\"").append(",");
            }, () -> {
                builder.append(new JsonObject(o)).append(",");
            });
        }
        return ((builder.length() > 2) ? builder.deleteCharAt(builder.length() - 1) : builder).append("]").toString();
    }


    private static String arrayToJSONString(Object object) {
        if (object instanceof byte[]) {
            return Arrays.toString((byte[]) object).replaceAll(" ", "");
        }
        if (object instanceof short[]) {
            return Arrays.toString((short[]) object).replaceAll(" ", "");
        }
        if (object instanceof int[]) {
            return Arrays.toString((int[]) object).replaceAll(" ", "");
        }
        if (object instanceof long[]) {
            return Arrays.toString((long[]) object).replaceAll(" ", "");
        }
        if (object instanceof float[]) {
            return Arrays.toString((float[]) object).replaceAll(" ", "");
        }
        if (object instanceof double[]) {
            return Arrays.toString((double[]) object).replaceAll(" ", "");
        }
        if (object instanceof boolean[]) {
            return Arrays.toString((boolean[]) object);
        }
        if (object instanceof char[]) {
            return charArrayToJSONString((char[]) object);
        }
        if (object instanceof Object[]) {
            return objectArrayToJSONString((Object[]) object);
        }
        return null;
    }


    private static String collectionToJSONString(Collection<?> collection) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        collection.forEach(o -> {
            ifBasicType(o, () -> {
                builder.append(o).append(",");
            }, () -> {
                ifTextType(o, () -> {
                    builder.append("\"").append(o).append("\"").append(",");
                }, () -> {
                    builder.append(new JsonObject(o)).append(",");
                });
            });
        });
        return ((builder.length() > 2) ? builder.deleteCharAt(builder.length() - 1) : builder).append("]").toString();
    }


    private static String mapToJSONString(Map<?, ?> map) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        map.forEach((k, v) -> Optional.ofNullable((k != null && v != null) ? k : null).ifPresent(o -> {
            builder.append("\"").append(k).append("\":");
            ifBasicType(v, () -> {
                builder.append(v).append(",");
            }, () -> {
                ifTextType(v, () -> {
                    builder.append("\"").append(v).append("\"").append(",");
                }, () -> {
                    builder.append(new JsonObject(v)).append(",");
                });
            });
        }));
        return ((builder.length() > 2) ? builder.deleteCharAt(builder.length() - 1) : builder).append("}").toString();
    }


    private static void ifBasicType(Object o, Runnable runnable1, Runnable runnable2) {
        if (o instanceof Byte || o instanceof Short || o instanceof Integer
                || o instanceof Long || o instanceof Boolean || o instanceof Float
                || o instanceof Double || o instanceof BigInteger || o instanceof BigDecimal) {
            Optional.ofNullable(runnable1).ifPresent(Runnable::run);
        } else {
            Optional.ofNullable(runnable2).ifPresent(Runnable::run);
        }
    }


    private static void ifTextType(Object o, Runnable runnable1, Runnable runnable2) {
        if (o instanceof String || o instanceof Character) {
            Optional.ofNullable(runnable1).ifPresent(Runnable::run);
        } else {
            Optional.ofNullable(runnable2).ifPresent(Runnable::run);
        }
    }

}