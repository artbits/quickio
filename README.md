# QuickIO
[![](https://www.jitpack.io/v/artbits/quickio.svg)](https://www.jitpack.io/#artbits/quickio)


QuickIO is a Java library designed based on LevelDB embedded database. It can quickly store or read Java beans in disk, zero configuration, fast and efficient.


## Download
Gradle:
```gradle
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
	implementation 'com.github.artbits:quickio:0.0.2'
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
	<version>0.0.2</version>
</dependency>
```


## How do I use QuickIO?
1. You must initialize before using QuickIO.
```java
QuickIO.init();
```

2. Create a java bean that needs to be stored or read, and *extends* the **IObject** class.
```java
public class Book extends IObject {
    
    private String name;
    private String author;
    private double price;
    private int pages;
    
    //Getter and Setter
    
}
```

3. CURD with QuickIO.

+ Save the java bean.
```java
Book book = new Book();
book.setName("C Primer Plus");
book.setAuthor("Stephen Prata");
book.setPrice(108.00);
book.setPages(541);

//The first way.
book.save();

//The second way.
QuickIO.save(book);

//Saved successfully. The value of ID is not zero.
System.out.println(book.id());

//Update the stored data according to the ID.
book.setPrice(50.10);
book.save();
book.setPrice(108.00);
QuickIO.save(book);
```

+ Update Java bean according to conditions.
```java
Book book = new Book();
book.setName("C Primer Plus");
book.setAuthor("Stephen Prata");
book.setPrice(249.99);

//Single condition
QuickIO.update(book, options -> options.$eq("author", "Stephen Prata"));

//Multiple conditions
QuickIO.update(book, options -> {
    options.$eq("name", "C Primer Plus");
    options.$eq("author", "Stephen Prata");
});
```

+ Delete the Jave bean. The deletion succeeds. The return value of the function is true, otherwise it is false, except for the conditional deletion function.
```java
//The first way.
boolean b1 = book.delete();
System.out.println(b1);

//The second way.
boolean b2 = QuickIO.delete(book.id());
System.out.println(b2);

//The third way.
QuickIO.delete(Book.class, options -> {
    options.$eq("name", "C Primer Plus");
    options.$eq("author", "Stephen Prata");
});
```

+ Find Java bean.
```java
//Find the first java bean of type book.
Book book1 = QuickIO.findFirst(Book.class);

//Find the last java bean of type book.
Book book2 = QuickIO.findLast(Book.class);

//Find the java bean of book type with the specified ID.
Book book3 = QuickIO.find(Book.class, 1001657291650502656L);

//Find the first java bean of book type by criteria.
Book book4 = QuickIO.findOne(Book.class, options -> options.$eq("name", "C Primer Plus"));

//Find all Java beans of book type.
List<Book> books1 = QuickIO.find(Book.class);

//Batch find Java beans of book type by ID.
List<Book> books2 = QuickIO.find(Book.class, id1, id2, id3, id4);

//Batch find Java beans of book type by conditions.
List<Book> books3 = QuickIO.find(Book.class, options -> options.$eq("name", "C Primer Plus"));

//For complex queries, use custom functions to find Java beans of book type, 
// and return true to indicate that they meet the conditions, 
// and return false to indicate that they do not meet the conditions.
List<Book> books4 = QuickIO.findCustom(Book.class, book -> "C Primer Plus".equals(book.getName()) 
        || book.getPrice() > 25);
```

+ Function description of the **Options** class.

|      |     |          |
|:----:|:---:|:--------:|
| Function name | Explain | Support scope       |
| $eq  | Be equal to                          | Save, Update, Delete, Find |
| $lt  | Less than                            | Save, Update, Delete, Find |
| $lte | Less than and equal to               | Save, Update, Delete, Find |
| $gt  | Greater than                         | Save, Update, Delete, Find |
| $gte | Greater than and equal to            | Save, Update, Delete, Find |
| $ne  | Not equal to                         | Save, Update, Delete, Find |
| sort | Sort, 1 is asc, and -1 is desc       | Find |
| limit| Limit the number of search results   | Find |


## Thanks
The open source libraries used by **QuickIO** include [LevelDB](https://github.com/dain/leveldb) and [Hessian](http://hessian.caucho.com/).


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