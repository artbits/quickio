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
import java.util.function.Predicate;

class QuickCan {

    private final String path;


    QuickCan(String path) {
        if (path == null || path.isEmpty()) {
            throw new RuntimeException("The parameter cannot be null or empty");
        }
        try {
            this.path = path;
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
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