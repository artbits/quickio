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

import com.github.artbits.quickio.api.JDB;
import com.github.artbits.quickio.api.JKV;
import com.github.artbits.quickio.api.JTin;

public final class QuickIO extends Plugin {

    public static JDB db(String name) {
        return new QDB(name);
    }


    public static JDB db(Config config) {
        return new QDB(config);
    }


    public static JKV kv(String name) {
        return new QKV(name);
    }


    public static JKV kv(Config config) {
        return new QKV(config);
    }


    public static JTin tin(String name) {
        return new QTin(name);
    }


    public static JTin tin(Config config) {
        return new QTin(config);
    }

}