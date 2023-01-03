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

import java.io.Serializable;

public final class QuickIO {

    private final static String DB_PATH = "data/db/";
    private final static String KV_PATH = "data/kv/";
    private final static String CAN_PATH = "data/can/";
    private final static Snowflake snowflake = new Snowflake(0, 0);


    public static class Object implements Serializable {
        long id;

        public final long id() {
            return id;
        }

        public final long timestamp() {
            return toTimestamp(id);
        }

        public final String toJson() {
            return QuickIO.toJson(this);
        }

        public final void printJson() {
            Tools.printJson(this);
        }
    }


    public static class DB extends QuickDB {
        public DB(String name) {
            super((name == null || name.isEmpty()) ? null : DB_PATH + name);
        }
    }


    public static class KV extends QuickKV {
        public KV(String name) {
            super((name == null || name.isEmpty()) ? null : KV_PATH + name);
        }
    }


    public static class Can extends QuickCan {
        public Can(String name) {
            super((name == null || name.isEmpty()) ? null : CAN_PATH + name);
        }
    }


    public static long id() {
        return snowflake.nextId();
    }


    public static long toTimestamp(long id) {
        return snowflake.toTimestamp(id);
    }


    public static <T> String toJson(T t) {
        return new JSONObject(t).toString();
    }


    public static <T> void printJson(T t) {
        Tools.printJson(t);
    }


    public static void print(java.lang.Object o) {
        Tools.print(o);
    }


    public static void print(String s, java.lang.Object... args) {
        Tools.print(String.format(s, args));
    }


    public static void println(java.lang.Object o) {
        Tools.println(o);
    }


    public static void println(String s, java.lang.Object... args) {
        Tools.println(s, args);
    }

}