# QuickIO
[![](https://www.jitpack.io/v/artbits/quickio.svg)](https://www.jitpack.io/#artbits/quickio)


Quickio is a Java library based on LevelDB embedded database design. It can quickly read or write Java beans in the disk, or store data as a K-V database. Zero configuration, fast and efficient.


## Download
Gradle:
```gradle
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.artbits:quickio:0.0.6'
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
    <version>0.0.6</version>
</dependency>
```


## How do I use QuickIO?

### 1. Store Java beans.
Create a Java bean that needs to be stored or read, and extends the **IObject** class.
```java
public class Book extends IObject {
    private String name;
    private String author;
    private Float price;
    private Integer pages;
    //Getter and Setter
}
```

Start using.
```java
//Create QuickIO.Store object and set store directory.
QuickIO.Store store = QuickIO.store("simple_store");



//Save:
//Create Book object and set data.
Book book = new Book();
book.setName("C Primer Plus");
book.setAuthor("Stephen Prata");
book.setPrice(108.00f);
book.setPages(541);

//Save data.
store.save(book);

//Saved successfully. The value of ID is not zero.
System.out.println(book.id());
//Save succeeded. Get the timestamp when saving.
System.out.println(book.timestamp());

//Update the stored data according to the ID.
book.setPrice(50.10f);
store.save(book);

//Batch save data.
List<Book> books = new ArrayList<>();
books.add(book);
books.add(book);
books.add(book);
store.save(books);
books.forEach(b -> System.out.println(b.id()));



//Update:
//New a Book object and set the data to be modified.
Book book = new Book();
book.setPrice(249.99f);

//Update data by condition.
store.update(book, b -> {
    boolean b1 = Objects.equals(b.getName(), "C Primer Plus");
    boolean b2 = Objects.equals(b.getAuthor(), "Stephen Prata");
    return b1 && b2;
});



//Delete:
//Delete by ID. Deletion succeeded, the result is true.
boolean res = store.delete(book.id());
System.out.println(res);

//Batch delete by ID.
store.delete(id1, id2, id3, id4);

//Delete all data of Book type.
store.delete(Book.class);

//Delete by condition.
store.delete(Book.class, b -> Objects.equals(b.getName(), "C Primer Plus"));



//Find:
//Find the first Java bean of type Book.
Book book1 = store.findFirst(Book.class);

//Find the last Java bean of type Book.
Book book2 = store.findLast(Book.class);

//Find the first Java bean of Book type by criteria.
Book book3 = store.findOne(Book.class, b -> Objects.equals(b.getName(), "C Primer Plus"));

//Find the Java bean of Book type with the specified ID.
Book book4 = store.find(Book.class, 1001657291650502656L);

//Find all Java beans of Book type.
List<Book> books1 = store.find(Book.class);

//Batch find Java beans of Book type by ID.
List<Book> books2 = store.find(Book.class, id1, id2, id3, id4);

//Batch find Java beans of Book type by conditions.
List<Book> books3 = store.find(Book.class, b -> Objects.equals(b.getName(), "C Primer Plus"));

//Batch find Java beans of Book type by conditions.
//Sort, 1 is asc, and -1 is desc.
//Can limit the quantity.
List<Book> books4 = store.find(Book.class, b -> {
    boolean b1 = Objects.equals(b.getName(), "C Primer Plus");
    boolean b2 = Objects.equals(b.getAuthor(), "Stephen Prata");
    return b1 && b2;
}, options -> {
    options.sort("price", 1);
    options.limit(10);
});



//Destroy objects manually.
store.destroy();
```

### 2. Store K-V type data.
Start using.
```java
//Create QuickIO.KV object and set store directory
QuickIO.KV kv = QuickIO.kv("simple_kv");



//Save basic type data and infer type by value.
kv.write("Int", 2022);
kv.write("Long", 1015653787903332352L);
kv.write("Float", 3.14f);
kv.write("Double", 3.141592654d);
kv.write("Bool", true);
kv.write("Char", 'c');
kv.write("String", "Hello world!");

//Read basic type data and infer type by default value.
int i = kv.read("Int", 0);
long l = kv.read("Long", 0L);
float f = kv.read("Float", 0f);
double d = kv.read("Double", 0d);
boolean b = kv.read("Bool", false);
char c = kv.read("Char", '0');
String s = kv.read("String", "");

//Remove the specified key, true if the removal is successful, false otherwise.
boolean b1 = kv.remove("Int");

//Query whether the key exists, if it exists, it is true, otherwise it is false.
boolean b2 = kv.containsKey("Long");



//Store Java beans.
//Create object and use the serializable interface.
public class User implements Serializable {
    String name;
    Integer age;

    public User(Consumer<User> consumer) {
        consumer.accept(this);
    }
}

//Save Java beans data.
kv.write("Li Ming", new User(u -> {
    u.name = "Li Ming";
    u.age = 18;
}));

//Read Java beans data.
User user = kv.read("Li Ming", User.class);
if (user != null) {
    System.out.println(user.name + " " + user.age);
}



//Destroy objects manually.
kv.destroy();
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