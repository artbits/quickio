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
    implementation 'com.github.artbits:quickio:1.1.7'
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
    <version>1.1.7</version>
</dependency>
```


## How do I use QuickIO?

### 1. Store Java beans.
Create a Java bean that needs to be stored or read, and extends the ``QuickIO.Object`` class.
```java
public class User extends QuickIO.Object {
    public Integer age;
    public String name;
    public String gender;
    public String email;

    public User(Consumer<User> consumer) {
        consumer.accept(this);
    }
}
```

Start using.
```java
//Create QuickIO.DB object and set store directory.
QuickIO.DB db = new QuickIO.DB("sample_db");



//Save:
//Create User object and set data.
User user = new User(u -> {
    u.name = "LiMing";
    u.age = 18;
    u.gender = "male";
    u.email = "liming@foxmail.com";
});

//Save data.
db.save(user);

//Saved successfully. The value of ID is not zero.
System.out.println(user.id());
//Saved successfully. Get the timestamp when saving.
System.out.println(user.timestamp());
//Java bean to json.
System.out.println(user.toJson());

//Update the stored data according to the ID.
user.age = 20;
db.save(user);

//Batch save data.
List<User> users = Arrays.asList(user1, user2, user3);
db.save(users);
users.forEach(u -> System.out.println(u.id()));



//Update:
//New a User object and set the data to be modified.
User user = new User(u -> u.age = 25);

//Update data by condition.
db.update(user, u -> {
    boolean b1 = Objects.equals(u.name, "LiMing");
    boolean b2 = Objects.equals(u.email, "liming@foxmail.com");
    return b1 && b2;
});



//Delete:
//Delete by ID. Deletion succeeded, the result is true.
boolean res = db.delete(user.id());
System.out.println(res);

//Batch delete by ID.
db.delete(id1, id2, id3, id4);

//Batch delete by list(element must have an id).
db.delete(users);

//Delete all data of User type.
db.delete(User.class);

//Delete by condition.
db.delete(User.class, u -> u.age >= 16 && u.age <= 18);



//Find:
//Find the first Java bean of type User.
User user1 = db.findFirst(User.class);

//Find the first Java bean of User type by condition.
User user2 = db.findFirst(User.class, u -> u.age >= 18);

//Find the last Java bean of type Book.
User user3 = db.findLast(User.class);

//Find the last Java bean of User type by condition.
User user4 = db.findLast(User.class, u -> u.age >= 18);

//Find Java beans with unique User type by conditions.
User user5 = db.findOne(User.class, u -> "liming@gmail.com".equals(u.email));

//Find the Java bean of User type with the specified ID.
User user6 = db.find(User.class, 1001657291650502656L);

//Find all Java beans of User type.
List<User> users1 = db.find(User.class);

//Batch find Java beans of User type by ID.
List<User> users2 = db.find(User.class, id1, id2, id3, id4);

//Batch find Java beans of User type by conditions.
List<User> users3 = db.find(User.class, u -> u.age >= 18);

//Batch find Java beans of User type by conditions.
//Sort, 1 is asc, and -1 is desc.
//The number of skipped elements can be set.
//The number of limit elements can be set.
List<User> users4 = db.find(User.class, u -> {
    boolean b1 = u.gender.equals("male");
    boolean b2 = u.email.contains("@gmail.com");
    return b1 && b2;
}, options -> {
    options.sort("age", 1).skip(3).limit(10);
});

//The find condition can be null. Only the FindOptions parameter is set.
List<User> users5 = db.find(User.class, null, options -> {
    options.sort("age", 1).skip(3).limit(10);
});



//Conut:
//Count the number of User type data.
int res1 = db.count(User.class);

//Count the number of User type data by condition.
int res2 = db.count(User.class, u -> u.age >= 18);



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
    public String name;
    public Integer age;

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