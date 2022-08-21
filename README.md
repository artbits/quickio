# QuickIO
[![](https://www.jitpack.io/v/artbits/quickio.svg)](https://www.jitpack.io/#artbits/quickio)


QuickIO is a Java library designed based on LevelDB embedded database. It can quickly read or write Java beans in disk, zero configuration, fast and efficient.


## Download
Gradle:
```gradle
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.artbits:quickio:0.0.3'
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
    <version>0.0.3</version>
</dependency>
```


## How do I use QuickIO?
### 1. Initialization and destroy.
```java
//Initialize, open data file.
QuickIO.init();

//Another initialization, Customize the data file directory and open it.
QuickIO.init("dir");

//Destroy, manually release the operation data object.
//If this method is not used, the JVM will automatically call it.
QuickIO.destroy();
```

### 2. Create a Java bean that needs to be stored or read, and *extends* the IObject class.
```java
public class Book extends IObject {

    private String name;
    private String author;
    private float price;
    private int pages;
    
    //Getter and Setter

}
```

### 3. Official start.

Save:
```java
Book book = new Book();
book.setName("C Primer Plus");
book.setAuthor("Stephen Prata");
book.setPrice(108.00f);
book.setPages(541);

//Save data.
QuickIO.save(book);

//Saved successfully. The value of ID is not zero.
System.out.println(book.id());
//Save succeeded. Get the timestamp when saving.
System.out.println(book.timestamp());

//Update the stored data according to the ID.
book.setPrice(50.10f);
QuickIO.save(book);

//Batch save data.
List<Book> books = new ArrayList<>();
books.add(book);
books.add(book);
books.add(book);
QuickIO.save(books);
books.forEach(b -> System.out.println(b.id()));
```

Update:
```java
Book book = new Book();
book.setPrice(249.99f);

//Update data by condition.
QuickIO.update(book, b -> {
    boolean b1 = Objects.equals(b.getName(), "C Primer Plus");
    boolean b2 = Objects.equals(b.getAuthor(), "Stephen Prata");
    return b1 && b2;
});
```

Delete:
```java
//Delete by ID. Deletion succeeded, the result is true.
boolean res = QuickIO.delete(book.id());
System.out.println(res);

//Batch delete by ID.
QuickIO.delete(id1, id2, id3, id4);

//Delete all data of Book type.
QuickIO.delete(Book.class);

//Delete by condition.
QuickIO.delete(Book.class, b -> Objects.equals(b.getName(), "C Primer Plus"));
```

Find:
```java
//Find the first Java bean of type Book.
Book book1 = QuickIO.findFirst(Book.class);

//Find the last Java bean of type Book.
Book book2 = QuickIO.findLast(Book.class);

//Find the first Java bean of Book type by criteria.
Book book3 = QuickIO.findOne(Book.class, b -> Objects.equals(b.getName(), "C Primer Plus"));

//Find the Java bean of Book type with the specified ID.
Book book4 = QuickIO.find(Book.class, 1001657291650502656L);

//Find all Java beans of Book type.
List<Book> books1 = QuickIO.find(Book.class);

//Batch find Java beans of Book type by ID.
List<Book> books2 = QuickIO.find(Book.class, id1, id2, id3, id4);

//Batch find Java beans of Book type by conditions.
List<Book> books3 = QuickIO.find(Book.class, b -> Objects.equals(b.getName(), "C Primer Plus"));

//Batch find Java beans of Book type by conditions.
//Sort, 1 is asc, and -1 is desc.
//Can limit the quantity.
List<Book> books4 = QuickIO.find(Book.class, b -> {
    boolean b1 = Objects.equals(b.getName(), "C Primer Plus");
    boolean b2 = Objects.equals(b.getAuthor(), "Stephen Prata");
    return b1 && b2;
}, options -> {
    options.sort("price", 1);
    options.limit(10);
});
```


## Simple
[Here](https://github.com/artbits/quickio/tree/main/src/test/java/simple)


## Thanks
Open source projects used by QuickIO.
+ [LevelDB](https://github.com/dain/leveldb)
+ [Hessian](http://hessian.caucho.com/)


# License
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