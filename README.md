[![](https://www.jitpack.io/v/artbits/quickio.svg)](https://www.jitpack.io/#artbits/quickio)
[![](https://img.shields.io/badge/JDK-8%20%2B-%23DD964D)](https://jdk.java.net/)
[![](https://img.shields.io/badge/license-Apache--2.0-%234377BF)](#license)


## QuickIO
QuickIO is a Java embedded database. The underlying layer is based on the ``LevelDB`` engine and Java NIO design, and uses ``Protostaff`` to serialize/deserialize data. Support the storage of document, key-value and file type data. Directly using Java code to operate databases is simple, flexible, and efficient.


## Features
+ Embedded databases like ``SQLite`` do not need to be installed or independent processes.
+ NoSQL databases like ``MongoDB`` or ``Diskv`` are very simple to use.
+ Support the storage of document, key-value and file type data. 
+ Unique index is supported to meet the requirement of fast query.
+ Simple API, elegant operation using Java Lambda expressions.
+ Fast reading and writing to meet the use scenarios of small and medium-sized data.


## Discover
[SQLite-Java](https://github.com/artbits/sqlite-java) is a Java ORM for SQLite databases. Written by the author with reference to the ``QuickIO`` project. It is currently in the development stage, welcome to watch.


## Download
Gradle:
```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.artbits:quickio:1.3.4'
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
    <version>1.3.4</version>
</dependency>
```
Download the Jar file, please click [here](/downloads/).

## Usage
Store data of document type.
```java
DB db = QuickIO.usingDB("example_db")
Collection<Document> collection = db.collection(Document.class);

collection.save(new Document().put("city", "Canton").put("area", 7434.4));

Document document = collection.findOne(d -> "Canton".equals(d.get("city")));
Optional.ofNullable(document).ifPresent(IOEntity::printJson);
```
Custom entity classes are stored according to the data of document type.
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


DB db = QuickIO.usingDB("example_db")
Collection<Book> collection = db.collection(Book.class);

collection.save(Book.of(b -> {
    b.name = "On java 8";
    b.author = "Bruce Eckel";
    b.price = 129.8;
}));

List<Book> books = collection.findAll();
books.forEach(IOEntity::printJson);
```
Store data of Key-Value type, and support any key and value that can be serialized and deserialized.
```java
KV kv = QuickIO.usingKV("example_kv")
kv.write("Pi", 3.14);
kv.write(3.14, "Pi");

double d = kv.read("Pi", Double.class);
String s = kv.read(3.14, String.class);
QuickIO.println("%s = %f", s, d);
```
Stores data for file types.
```java
Tin tin = QuickIO.usingTin("example_tin")
tin.put("photo.png", new File("..."));

File file = tin.get("photo.png");
Optional.ofNullable(file).ifPresent(f -> QuickIO.println(f.getPath()));
```


## Links
+ APIs:
    + [DB  - Document storage](/src/test/java/apis/DBExample.java)
    + [KV  - Key-Value storage](/src/test/java/apis/KVExample.java)
    + [Tin - File storage](/src/test/java/apis/TinExample.java)
+ Thanks: 
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