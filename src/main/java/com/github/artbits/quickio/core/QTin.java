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

import com.github.artbits.quickio.api.Tin;
import com.github.artbits.quickio.exception.QIOException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static com.github.artbits.quickio.core.Constants.TIN_PATH;

final class QTin implements Tin {

    private final String LOCK_FILE_NAME = ".LOCK";

    private final String path;
    private FileLock lock;
    private FileChannel lockChannel;


    QTin(Config config) {
        if (config.name == null || config.name.isEmpty()) {
            throw new QIOException(Constants.ILLEGAL_NAME);
        } else if (config.name.contains("/")) {
            throw new QIOException(Constants.SPECIAL_CHARACTER_NAME);
        }
        if (config.path == null) {
            path = Paths.get(TIN_PATH, config.name).toString();
        } else {
            path = Paths.get(config.path, TIN_PATH, config.name).toAbsolutePath().toString();
        }
        try {
            Files.createDirectories(Paths.get(path));
            if (!new File(path + "/" + LOCK_FILE_NAME).exists()) {
                Files.createFile(Paths.get(path, LOCK_FILE_NAME));
            }
            lockChannel = new FileOutputStream(path + "/" + LOCK_FILE_NAME,true).getChannel();
            lock = lockChannel.lock();
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        } catch (IOException e) {
            throw new QIOException(e);
        }
    }


    QTin(String name) {
        this(Config.of(c -> c.name(name)));
    }


    @Override
    public void close() {
        try {
            if (lock != null) {
                lock.release();
                lock = null;
            }
        } catch (IOException e) {
            throw new QIOException(e);
        }
        try {
            if (lockChannel != null) {
                lockChannel.close();
                lockChannel = null;
            }
        } catch (IOException e) {
            throw new QIOException(e);
        }
    }


    @Override
    public void destroy() {
        try {
            close();
            Path filePath = Paths.get(path);
            Comparator<Path> comparator = Comparator.reverseOrder();
            Files.walk(filePath).sorted(comparator).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new QIOException(e);
                }
            });
        } catch (IOException e) {
            throw new QIOException(e);
        }
    }


    @Override
    public void put(String filename, File file) {
        if (!LOCK_FILE_NAME.equals(filename)) {
            String outPath = path + "/" + filename;
            try (FileChannel inChannel = FileChannel.open(Paths.get(file.getPath()), StandardOpenOption.READ);
                 FileChannel outChannel = FileChannel.open(Paths.get(outPath), StandardOpenOption.READ,
                         StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } catch (IOException e) {
                throw new QIOException(e);
            }
        }
    }


    @Override
    public void put(String filename, byte[] bytes) {
        if (!LOCK_FILE_NAME.equals(filename)) {
            String outPath = path + "/" + filename;
            try (FileChannel outChannel = FileChannel.open(Paths.get(outPath), StandardOpenOption.READ,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    outChannel.write(buffer);
                }
            } catch (IOException e) {
                throw new QIOException(e);
            }
        }
    }


    @Override
    public File get(String filename) {
        if (!LOCK_FILE_NAME.equals(filename)) {
            File file = new File(path + "/" + filename);
            return file.exists() ? file : null;
        }
        return null;
    }


    @Override
    public void remove(String filename) {
        try {
            if (!LOCK_FILE_NAME.equals(filename)) {
                Files.delete(Paths.get(path + "/" + filename));
            }
        } catch (NoSuchFileException e) {

        } catch (IOException e) {
            throw new QIOException(e);
        }
    }


    @Override
    public List<File> list() {
        List<File> fileList = new ArrayList<>();
        File[] files = new File(path).listFiles();
        for (File file : files) {
            if(file.isDirectory()) {
                continue;
            }
            if (LOCK_FILE_NAME.equals(file.getName())) {
                continue;
            }
            fileList.add(file);
        }
        return fileList;
    }


    @Override
    public void foreach(Predicate<File> predicate) {
        File[] files = new File(path).listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            if (LOCK_FILE_NAME.equals(file.getName())) {
                continue;
            }
            if (!predicate.test(file)) {
                break;
            }
        }
    }

}