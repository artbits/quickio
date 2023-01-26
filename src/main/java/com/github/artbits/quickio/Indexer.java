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

import com.github.artbits.quickio.annotations.Index;
import org.iq80.leveldb.WriteBatch;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.github.artbits.quickio.Tools.*;

final class Indexer extends LevelIO {

    private static class IndexObject implements Serializable {
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
            return "(" + className + "," + filedName + "," + value + ")";
        }
    }


    private static class IndexMapObject implements Serializable {
        Map<String, String> indexMap = new HashMap<>();
    }


    Indexer(String basePath, String name) {
        QuickIO.Options options = new QuickIO.Options();
        options.name = Constants.INDEX;
        options.basePath = basePath + name + "/";
        options.cacheSize = 10L * 1024 * 1024;
        open(options);
    }


    private <T extends QuickIO.Object> void setIndex(WriteBatch batch, T t) {
        List<IndexObject> indexObjects = extractIndexObjects(t);
        if (indexObjects.size() == 0) return;
        long value1 = t.id();
        long key2 = t.id();
        byte[] valueBytes2 = get(asBytes(key2));
        AtomicReference<IndexMapObject> atomicReference = new AtomicReference<>();
        if (valueBytes2 != null) {
            IndexMapObject indexMapObject = asObject(valueBytes2, IndexMapObject.class);
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
                batch.put(asBytes(key1), asBytes(value1));
            }
            if (!key1.equals(oldKey1)) {
                value2.indexMap.put(indexObject.filedName, key1);
                batch.put(asBytes(key1), asBytes(value1));
                Optional.ofNullable(oldKey1).ifPresent(key -> batch.delete(asBytes(key)));
            }
        });
        batch.put(asBytes(key2), asBytes(value2));
    }


    <T extends QuickIO.Object> void setIndex(T t) {
        writeBatch(batch -> setIndex(batch, t));
    }


    <T extends QuickIO.Object> void setIndexes(List<T> list) {
        if (list.size() < 1) return;
        if (getAnnotationFields(list.get(0)).size() == 0) return;
        Map<String, Boolean> guardMap = new HashMap<>();
        list.forEach(t -> {
            List<IndexObject> indexObjects = extractIndexObjects(t);
            indexObjects.forEach(indexObject -> {
                String key1 = indexObject.toString();
                if (guardMap.getOrDefault(key1, false)) {
                    throw new RuntimeException(key1 + " already exists");
                } else {
                    guardMap.put(key1, true);
                }
            });
        });
        writeBatch(batch -> list.forEach(t -> setIndex(batch, t)));
    }


    void removeIndex(long id) {
        if (id == 0) return;
        byte[] valueBytes2 = get(asBytes(id));
        if (valueBytes2 != null) {
            writeBatch(batch -> {
                IndexMapObject indexMapObject = asObject(valueBytes2, IndexMapObject.class);
                indexMapObject.indexMap.values().forEach(key1 -> batch.delete(asBytes(key1)));
                batch.delete(asBytes(id));
            });
        }
    }


    <T extends QuickIO.Object> void removeIndex(T t) {
        removeIndex(t.id());
    }


    void removeIndexes(long... ids) {
        writeBatch(batch -> {
            for (long id : ids) {
                byte[] valueBytes2 = get(asBytes(id));
                if (valueBytes2 != null) {
                    IndexMapObject value2 = asObject(valueBytes2, IndexMapObject.class);
                    value2.indexMap.values().forEach(key1 -> batch.delete(asBytes(key1)));
                    batch.delete(asBytes(id));
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


    <T extends QuickIO.Object> void removeIndexList(List<T> list) {
        List<Long> ids = list.stream().map(QuickIO.Object::id).filter(id -> id != 0).collect(Collectors.toList());
        removeIndexes(ids);
    }


    <T extends QuickIO.Object> void dropIndex(List<T> list, String fieldName) {
        writeBatch(batch -> list.forEach(t -> {
            byte[] valueBytes2 = get(asBytes(t.id()));
            if (valueBytes2 != null) {
                IndexMapObject value2 = asObject(valueBytes2, IndexMapObject.class);
                String key1 = value2.indexMap.getOrDefault(fieldName, null);
                Optional.ofNullable(key1).ifPresent(key -> batch.delete(asBytes(key)));
            }
        }));
    }


     long getIndexId(Class<?> tClass, String fieldName, Object filedValue) {
        inspectIndexField(tClass, fieldName);
        IndexObject indexObject = new IndexObject(tClass.getSimpleName(), fieldName, filedValue);
        String key1 = indexObject.toString();
        byte[] keyBytes1 = asBytes(key1);
        byte[] valueBytes1 = get(keyBytes1);
        return (valueBytes1 != null) ? asLong(valueBytes1) : 0;
    }


    boolean exist(Class<?> tClass, String fieldName, Object filedValue) {
        return getIndexId(tClass, fieldName, filedValue) != 0;
    }


    private void inspectIndexField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            boolean b = field.isAnnotationPresent(Index.class);
            Optional.ofNullable(b ? b : null).orElseThrow(() -> new RuntimeException("Non indexed field"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    private <T extends QuickIO.Object> List<IndexObject> extractIndexObjects(T t) {
        List<IndexObject> indexObjects = new ArrayList<>();
        List<Field> fields = getAnnotationFields(t);
        String className = t.getClass().getSimpleName();
        for (Field field : fields) {
            String fieldName = field.getName();
            Object fieldValue = getFieldValue(t, field);
            if (fieldValue == null) continue;
            IndexObject indexObject = new IndexObject(className, fieldName, fieldValue);
            String key1 = indexObject.toString();
            byte[] valueBytes1 = get(asBytes(key1));
            if (valueBytes1 != null) {
                long value1 = asLong(valueBytes1);
                if (value1 != t.id()) {
                    throw new RuntimeException(key1 + " already exists");
                }
            }
            indexObjects.add(indexObject);
        }
        return indexObjects;
    }

}