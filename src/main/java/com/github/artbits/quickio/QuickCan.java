package com.github.artbits.quickio;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class QuickCan {

    private final String path;


    QuickCan(String path) {
        try {
            this.path = path;
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void put(String filename, File file) {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = FileChannel.open(Paths.get(file.getPath()), StandardOpenOption.READ);
            outChannel = FileChannel.open(
                    Paths.get(path + "/" + filename),
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE
            );
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
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


    public void destroy() {
        try {
            Files.walk(Paths.get(path))
                    .sorted(Comparator.reverseOrder())
                    .forEach(path1 -> {
                        try {
                            Files.delete(path1);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}