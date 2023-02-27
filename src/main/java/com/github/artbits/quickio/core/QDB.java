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

import com.github.artbits.quickio.api.Collection;
import com.github.artbits.quickio.api.DB;

import java.nio.file.Paths;

import static com.github.artbits.quickio.core.Constants.DB_PATH;

final class QDB implements DB {

    private final EngineIO engine;
    private final Indexer indexer;


    QDB(Config config) {
        if (config.path == null) {
            config.path = DB_PATH;
        } else {
            config.path = Paths.get(config.path, DB_PATH).toAbsolutePath().toString();
        }
        engine = new EngineLevel().open(config);
        indexer = new Indexer(new EngineLevel(), config.path, config.name);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }


    QDB(String name) {
        this(Config.of(c -> c.name(name)));
    }


    @Override
    public void close() {
        engine.close();
        indexer.close();
    }


    @Override
    public void destroy() {
        engine.destroy();
        indexer.destroy();
    }


    @Override
    public <T extends IOEntity> Collection<T> collection(Class<T> clazz) {
        return new QCollection<>(clazz, engine, indexer);
    }

}