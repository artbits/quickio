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

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

class QuickCan {

    private final String path;


    QuickCan(String path) {
        try {
            this.path = Optional.ofNullable(path).orElseThrow(() -> new RuntimeException("The path cannot be null or empty"));
            Files.createDirectories(Paths.get(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void put(String filename, File file) {
        String outPath = path + "/" + filename;
        try (FileChannel inChannel = FileChannel.open(Paths.get(file.getPath()), StandardOpenOption.READ);
             FileChannel outChannel = FileChannel.open(Paths.get(outPath), StandardOpenOption.READ,
                     StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public File get(String filename) {
        File file = new File(path + "/" + filename);
        return file.exists() ? file : null;
    }


    public void remove(String filename) {
        try {
            Files.delete(Paths.get(path + "/" + filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public List<File> list() {
        List<File> fileList = new ArrayList<>();
        File[] files = new File(path).listFiles();
        for (File file : files) {
            if(file.isDirectory()) {
                continue;
            }
            fileList.add(file);
        }
        return fileList;
    }


    public void foreach(Predicate<File> predicate) {
        File[] files = new File(path).listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            if (!predicate.test(file)) {
                break;
            }
        }
    }


    public void destroy() {
        try {
            Path filePath = Paths.get(path);
            Comparator<Path> comparator = Comparator.reverseOrder();
            Files.walk(filePath).sorted(comparator).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}