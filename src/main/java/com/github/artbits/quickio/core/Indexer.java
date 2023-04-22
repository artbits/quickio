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
import com.github.artbits.quickio.exception.QIOException;
import org.iq80.leveldb.WriteBatch;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.artbits.quickio.core.Constants.INDEX;

final class Indexer {

    private final EngineIO engine;


    private static class IndexObject {
        String className;
        String filedName;
        Object value;

        IndexObject(String className, String filedName, Object value) {
            this.className = className;
            this.filedName = filedName;
            this.value = value;
        }

        @Override
        public String toString() {
            return "[" + className + "," + filedName + "," + value + "]";
        }
    }


    private static class IndexMapObject {
        Map<String, String> indexMap = new HashMap<>();
    }


    Indexer(EngineIO engine, String path, String name) {
        this.engine = engine.open(Config.of(c -> c.name(INDEX)
                .path(path + "/" + name)
                .cache(1024 * 1024L)));
    }


    void close() {
        engine.close();
    }


    void destroy() {
        engine.destroy();
    }


    private <T extends IOEntity> void setIndex(WriteBatch batch, T t) {
        List<IndexObject> indexObjects = extractIndexObjects(t);
        if (indexObjects.size() == 0) return;
        long value1 = t.objectId();
        long key2 = t.objectId();
        byte[] valueBytes2 = engine.get(Codec.encodeKey(key2));
        AtomicReference<IndexMapObject> atomicReference = new AtomicReference<>();
        if (valueBytes2 != null) {
            IndexMapObject indexMapObject = Codec.decode(valueBytes2, IndexMapObject.class);
            atomicReference.set(indexMapObject);
        }
        if (atomicReference.get() == null) {
            atomicReference.set(new IndexMapObject());
        }
        IndexMapObject value2 = atomicReference.get();
        indexObjects.forEach(indexObject -> {
            String key1 = indexObject.toString();
            String oldKey1 = value2.indexMap.getOrDefault(indexObject.filedName, null);
            if (oldKey1 == null) {
                value2.indexMap.put(indexObject.filedName, key1);
                batch.put(Codec.encode(key1), Codec.encodeKey(value1));
            }
            if (!key1.equals(oldKey1)) {
                value2.indexMap.put(indexObject.filedName, key1);
                batch.put(Codec.encode(key1), Codec.encodeKey(value1));
                Optional.ofNullable(oldKey1).ifPresent(key -> batch.delete(Codec.encode(key)));
            }
        });
        batch.put(Codec.encodeKey(key2), Codec.encode(value2));
    }


    <T extends IOEntity> void setIndex(T t) {
        engine.writeBatch(batch -> setIndex(batch, t));
    }


    <T extends IOEntity> void setIndexes(List<T> list) {
        if (list.size() < 1) {
            return;
        }
        if (!new ReflectObject<>(list.get(0)).containsAnnotation(Index.class)) {
            return;
        }
        Map<String, Boolean> guardMap = new HashMap<>();
        list.forEach(t -> {
            List<IndexObject> indexObjects = extractIndexObjects(t);
            indexObjects.forEach(indexObject -> {
                String key1 = indexObject.toString();
                if (guardMap.getOrDefault(key1, false)) {
                    throw new QIOException(key1 + Constants.INDEX_ALREADY_EXISTS);
                } else {
                    guardMap.put(key1, true);
                }
            });
        });
        engine.writeBatch(batch -> list.forEach(t -> setIndex(batch, t)));
    }


    void removeIndex(long id) {
        if (id == 0) return;
        byte[] valueBytes2 = engine.get(Codec.encodeKey(id));
        if (valueBytes2 != null) {
            engine.writeBatch(batch -> {
                IndexMapObject indexMapObject = Codec.decode(valueBytes2, IndexMapObject.class);
                indexMapObject.indexMap.values().forEach(key1 -> batch.delete(Codec.encode(key1)));
                batch.delete(Codec.encodeKey(id));
            });
        }
    }


    <T extends IOEntity> void removeIndex(T t) {
        removeIndex(t.objectId());
    }


    void removeIndexes(long... ids) {
        engine.writeBatch(batch -> {
            for (long id : ids) {
                byte[] valueBytes2 = engine.get(Codec.encodeKey(id));
                if (valueBytes2 != null) {
                    IndexMapObject value2 = Codec.decode(valueBytes2, IndexMapObject.class);
                    value2.indexMap.values().forEach(key1 -> batch.delete(Codec.encode(key1)));
                    batch.delete(Codec.encodeKey(id));
                }
            }
        });
    }


    void removeIndexes(List<Long> ids) {
        long[] idArray = new long[ids.size()];
        for (int i = 0, len = ids.size(); i < len; i++) {
            idArray[i] = ids.get(i);
        }
        removeIndexes(idArray);
    }


    <T extends IOEntity> void removeIndexList(List<T> list) {
        List<Long> ids = list.stream().map(IOEntity::objectId).filter(id -> id != 0).collect(Collectors.toList());
        removeIndexes(ids);
    }


    <T extends IOEntity> void dropIndex(List<T> list, String fieldName) {
        engine.writeBatch(batch -> list.forEach(t -> {
            byte[] valueBytes2 = engine.get(Codec.encode(t.objectId()));
            if (valueBytes2 != null) {
                IndexMapObject value2 = Codec.decode(valueBytes2, IndexMapObject.class);
                String key1 = value2.indexMap.getOrDefault(fieldName, null);
                Optional.ofNullable(key1).ifPresent(key -> batch.delete(Codec.encode(key)));
            }
        }));
    }


     long getIndexId(Class<?> tClass, String fieldName, Object filedValue) {
         try {
            if (tClass.getDeclaredField(fieldName).isAnnotationPresent(Index.class)) {
                IndexObject indexObject = new IndexObject(tClass.getSimpleName(), fieldName, filedValue);
                String key1 = indexObject.toString();
                byte[] keyBytes1 = Codec.encode(key1);
                byte[] valueBytes1 = engine.get(keyBytes1);
                return (valueBytes1 != null) ? Codec.decodeKey(valueBytes1) : 0;
            } else {
                throw new QIOException(Constants.NON_INDEXED_FIELD);
            }
        } catch (NoSuchFieldException e) {
            throw new QIOException(Constants.NON_INDEXED_FIELD);
        }
    }


    boolean exist(Class<?> tClass, String fieldName, Object filedValue) {
        return getIndexId(tClass, fieldName, filedValue) != 0;
    }


    private <T extends IOEntity> List<IndexObject> extractIndexObjects(T t) {
        String className = t.getClass().getSimpleName();
        List<IndexObject> indexObjects = new ArrayList<>();
        ReflectObject<T> reflectObject = new ReflectObject<>(t);
        reflectObject.traverseAnnotationFields(Index.class, (fieldName, fieldValue) -> {
            if (fieldValue == null) {
                return;
            }
            IndexObject indexObject = new IndexObject(className, fieldName, fieldValue);
            String key1 = indexObject.toString();
            byte[] valueBytes1 = engine.get(Codec.encode(key1));
            if (valueBytes1 != null) {
                long value1 = Codec.decodeKey(valueBytes1);
                if (value1 != t.objectId()) {
                    throw new QIOException(key1 + Constants.INDEX_ALREADY_EXISTS);
                }
            }
            indexObjects.add(indexObject);
        });
        return indexObjects;
    }

}