[![](https://www.jitpack.io/v/artbits/quickio.svg)](https://www.jitpack.io/#artbits/quickio)
[![](https://img.shields.io/badge/JDK-%3E%3D%208-orange)](https://jdk.java.net/)
[![](https://img.shields.io/badge/license-Apache--2.0-blue)](#license)

[English](README.md) | 中文


## QuickIO
QuickIO 是一个 Java 嵌入式数据库。底层基于 ``LevelDB`` 引擎和 Java NIO 设计，使用 ``Protostuff`` 序列化/反序列化数据。支持存储 **文档、key-value、文件** 类型的数据。直接使用 Java 代码操作数据库，简单高效。


## 特性
+ 像 ``SQLite`` 一样的嵌入式数据库，不需要安装，不需要独立进程。
+ 像 ``MongoDB`` 或 ``Diskv`` 一样的 NoSQL 数据库，使用十分简单。
+ 支持存储文档、key-value、文件类型的数据。
+ 支持**唯一索引**，以满足快速查询的要求。
+ 简易的 API，使用 Java Lambda 表达式优雅操作。
+ 读写快速，满足中小型数据量的使用场景。


## 下载
Gradle:
```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.artbits:quickio:1.3.2'
}
```
Maven:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://www.jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.artbits</groupId>
    <artifactId>quickio</artifactId>
    <version>1.3.2</version>
</dependency>
```


## 使用
存储文档类型的数据。
```java
try(DB db = QuickIO.usingDB("example_db")) {
    Collection<Document> collection = db.collection(Document.class);

    collection.save(new Document().put("city", "Canton").put("area", 7434.4));

    Document document = collection.findOne(d -> "Canton".equals(d.get("city")));
    Optional.ofNullable(document).ifPresent(IOEntity::printJson);
}
```
自定义实体类，按文档类型的数据进行存储。
```java
public class Book extends IOEntity {
    public String name;
    public String author;
    public Double price;
    
    public static Book of(Consumer<Book> consumer) {
        Book book = new Book();
        consumer.accept(book);
        return book;
    }
}


try(DB db = QuickIO.usingDB("example_db")) {
    Collection<Book> collection = db.collection(Book.class);

    collection.save(Book.of(b -> {
        b.name = "On java 8";
        b.author = "Bruce Eckel";
        b.price = 129.8;
    }));

    List<Book> books = collection.findAll();
    books.forEach(IOEntity::printJson);
}
```
存储 Key-Value 类型的数据，支持任意可序列化和反序列化的 key 和 value。
```java
try(KV kv = QuickIO.usingKV("example_kv")) {
    kv.write("Pi", 3.14);
    kv.write(3.14, "Pi");

    double d = kv.read("Pi", Double.class);
    String s = kv.read(3.14, String.class);
    QuickIO.println("%s = %f", s, d);
}
```
存储文件类型的数据。
```java
try(Tin tin = QuickIO.usingTin("example_tin")) {
    tin.put("photo.png", new File("..."));

    File file = tin.get("photo.png");
    Optional.ofNullable(file).ifPresent(f -> QuickIO.println(f.getPath()));
}
```


## 链接
+ APIs: 
    + [DB  - Document storage](/src/test/java/apis/DBExample.java)
    + [KV  - Key-Value storage](/src/test/java/apis/KVExample.java)
    + [Tin - File storage](/src/test/java/apis/TinExample.java)
+ 感谢: 
    + [LevelDB](https://github.com/dain/leveldb)
    + [Protostuff](https://github.com/protostuff/protostuff)


## License
```
Copyright 2022 Zhang Guanhu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```