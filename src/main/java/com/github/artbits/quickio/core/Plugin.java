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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Predicate;

class Plugin {

    private final static Snowflake snowflake = new Snowflake(0, 0);


    static int getDigit(long i){
        i = i > 0 ? i : -i;
        return i == 0 ? 1 : (int) Math.log10(i) + 1;
    }


    public static long generateId() {
        return snowflake.nextId();
    }


    public static long toTimestamp(long id) {
        return snowflake.toTimestamp(id);
    }


    public static String toDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    public static <T> String toJson(T t) {
        return new JsonObject(t).toString();
    }


    public static <T> void printJson(T t) {
        System.out.println(Optional.ofNullable(t)
                .map(s -> toJson(t))
                .orElse("The converted JSON object cannot be null"));
    }


    public static void print(Object o) {
        System.out.print(o);
    }


    public static void print(String s, Object... args) {
        System.out.printf(s, args);
    }


    public static void println(Object o) {
        System.out.println(o);
    }


    public static void println(String s, Object... args) {
        System.out.printf(s + "%n", args);
    }


    public static <T> byte[] encode(T t) {
        return Codec.encode(t);
    }


    public static <T> T decode(byte[] bytes, Class<T> clazz) {
        return Codec.decode(bytes, clazz);
    }


    public static <T> boolean bool(T value, Predicate<T> predicate) {
        return value != null && predicate.test(value);
    }

}