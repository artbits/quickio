# QuickIO
[![](https://www.jitpack.io/v/artbits/quickio.svg)](https://www.jitpack.io/#artbits/quickio)


QuickIO is a Java embedded database designed based on the LevelDB database engine. It can quickly read or write Java beans to disk, or store data as a K-V database, or store files in cans🥫. Zero configuration, fast and efficient.


## Download
Gradle:
```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.artbits:quickio:1.1.4'
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
    <version>1.1.4</version>
</dependency>
```


## How do I use QuickIO?

### 1. Store Java beans.
Create a Java bean that needs to be stored or read, and extends the ``QuickIO.Object`` class.
```java
public class Book extends QuickIO.Object {
    private String name;
    private String author;
    private Float price;
    private Integer pages;
    //Getter and Setter
}
```

Start using.
```java
//Create QuickIO.DB object and set store directory.
QuickIO.DB db = new QuickIO.DB("sample_db");



//Save:
//Create Book object and set data.
Book book = new Book();
book.setName("C Primer Plus");
book.setAuthor("Stephen Prata");
book.setPrice(108.00f);
book.setPages(541);

//Save data.
db.save(book);

//Saved successfully. The value of ID is not zero.
System.out.println(book.id());
//Saved successfully. Get the timestamp when saving.
System.out.println(book.timestamp());
//Java bean to json.
System.out.println(book.toJson());

//Update the stored data according to the ID.
book.setPrice(50.10f);
db.save(book);

//Batch save data.
List<Book> books = Arrays.asList(book1, book2, book3);
db.save(books);
books.forEach(b -> System.out.println(b.id()));



//Update:
//New a Book object and set the data to be modified.
Book book = new Book();
book.setPrice(249.99f);

//Update data by condition.
db.update(book, b -> {
    boolean b1 = Objects.equals(b.getName(), "C Primer Plus");
    boolean b2 = Objects.equals(b.getAuthor(), "Stephen Prata");
    return b1 && b2;
});



//Delete:
//Delete by ID. Deletion succeeded, the result is true.
boolean res = db.delete(book.id());
System.out.println(res);

//Batch delete by ID.
db.delete(id1, id2, id3, id4);

//Batch delete by list(element must have an id).
db.delete(books);

//Delete all data of Book type.
db.delete(Book.class);

//Delete by condition.
db.delete(Book.class, b -> Objects.equals(b.getName(), "C Primer Plus"));



//Find:
//Find the first Java bean of type Book.
Book book1 = db.findFirst(Book.class);

//Find the last Java bean of type Book.
Book book2 = db.findLast(Book.class);

//Find the first Java bean of Book type by criteria.
Book book3 = db.findOne(Book.class, b -> Objects.equals(b.getName(), "C Primer Plus"));

//Find the Java bean of Book type with the specified ID.
Book book4 = db.find(Book.class, 1001657291650502656L);

//Find all Java beans of Book type.
List<Book> books1 = db.find(Book.class);

//Batch find Java beans of Book type by ID.
List<Book> books2 = db.find(Book.class, id1, id2, id3, id4);

//Batch find Java beans of Book type by conditions.
List<Book> books3 = db.find(Book.class, b -> Objects.equals(b.getName(), "C Primer Plus"));

//Batch find Java beans of Book type by conditions.
//Sort, 1 is asc, and -1 is desc.
//Can limit the quantity.
List<Book> books4 = db.find(Book.class, b -> {
    boolean b1 = Objects.equals(b.getName(), "C Primer Plus");
    boolean b2 = Objects.equals(b.getAuthor(), "Stephen Prata");
    return b1 && b2;
}, options -> {
    options.sort("price", 1);
    options.limit(10);
});



//Manually close the database file.
db.close();

//Delete database file.
db.destroy();
```

### 2. Store K-V type data.
Start using.
```java
//Create QuickIO.KV object and set store directory.
QuickIO.KV kv = new QuickIO.KV("sample_kv");



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



//Manually close the database file.
kv.close();

//Delete database file.
kv.destroy();
```


### 3. Store file.
```java
//Create QuickIO.Can object and set store directory.
QuickIO.Can can = new QuickIO.Can("sample_can");

//Save the file to a can and change the file name.
can.put("test.png", new File("..."));

//Get the specified file from the can.
File file = can.get("test.png");
if (file != null) {
    System.out.println(file.getPath());
}

//Remove the specified file from the can.
can.remove("test.png");

//Traverse all files from a can.
List<File> files = can.list();

//Loop through the files in the can, 
//return true to continue the loop, 
//and return false to break the loop.
can.foreach(file -> {
    System.out.println(file.getName());
    return true;
});

//Delete the can.
can.destroy();
```


### 4. Widgets.
```java
//Get the unique ID and use Twitter's open-source
//distributed ID generation algorithm (Snowflake).
//Snowflake ID strongly depends on the machine clock.
long id = QuickIO.id();

//Get timestamp through Snowflake ID.
long timestamp = QuickIO.toTimestamp(id);
```


## Sample
[Here](https://github.com/artbits/quickio/tree/main/src/test/java/sample)


## Thanks
Open source projects used by QuickIO.
+ [LevelDB](https://github.com/dain/leveldb)
+ [Hessian](http://hessian.caucho.com/)
+ [JSON In Java](https://www.json.org/json-en.html)


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