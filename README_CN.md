[![](https://www.jitpack.io/v/artbits/quickio.svg)](https://www.jitpack.io/#artbits/quickio)
[![](https://img.shields.io/badge/JDK-%3E%3D%208-orange)](https://jdk.java.net/)
[![](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)

[English](README.md) | 中文

# QuickIO
QuickIO是基于LevelDB数据库引擎设计的Java嵌入式数据库。它可以快速地将Java bean读写到磁盘中，或作为K-V数据库进行数据存储，又或将文件存储在罐头中🥫。零配置，快速高效。


## 下载
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


## 如何使用QuickIO？

### 1. 存储Java bean
创建一个需要在磁盘中读写的Java bean，并继承 ``QuickIO.Object`` 类。
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

开始使用。
```java
//创建QuickIO.DB对象，并设置存储目录
QuickIO.DB db = new QuickIO.DB("sample_db");



//保存：
//创建User对象，并设置数据
User user = new User(u -> {
    u.name = "LiMing";
    u.age = 18;
    u.gender = "male";
    u.email = "liming@foxmail.com";
});

//保存数据
db.save(user);

//若保存成功，则ID值不为 0
System.out.println(user.id());
//若保存成功，则可获取到保存时的时间戳
System.out.println(user.timestamp());
//以Json数据格式打印Java bean
System.out.println(user.toJson());

//通过ID更新已存储的数据
user.age = 20;
db.save(user);

//批量保存数据
List<User> users = Arrays.asList(user1, user2, user3);
db.save(users);
users.forEach(u -> System.out.println(u.id()));



//更新：
//新建User对象，并设置要修改的数据
User user = new User(u -> u.age = 25);

//通过条件更新数据
db.update(user, u -> {
    boolean b1 = Objects.equals(u.name, "LiMing");
    boolean b2 = Objects.equals(u.email, "liming@foxmail.com");
    return b1 && b2;
});



//删除：
//通过ID删除；若删除成功，则返回值为true
boolean res = db.delete(user.id());
System.out.println(res);

//通过ID批量删除
db.delete(id1, id2, id3, id4);

//通过list批量删除（元素的ID值必须有效）
db.delete(users);

//删除User类型的全部数据
db.delete(User.class);

//按条件删除
db.delete(User.class, u -> u.age >= 16 && u.age <= 18);



//查找：
//查找第一个User类型的Java bean
User user1 = db.findFirst(User.class);

//按条件查找User类型的第一个Java bean
User user2 = db.findFirst(User.class, u -> u.age >= 18);

//查找最后一个User类型的Java bean
User user3 = db.findLast(User.class);

//按条件查找User类型的最后一个Java bean
User user4 = db.findLast(User.class, u -> u.age >= 18);

//按条件精确查找User类型的Java bean
User user5 = db.findOne(User.class, u -> "liming@gmail.com".equals(u.email));

//按ID查找User类型的Java bean
User user6 = db.find(User.class, 1001657291650502656L);

//查找User类型的所有Java bean
List<User> users1 = db.find(User.class);

//按ID批量查找User类型的Java bean
List<User> users2 = db.find(User.class, id1, id2, id3, id4);

//按条件批量查找User类型的Java bean
List<User> users3 = db.find(User.class, u -> u.age >= 18);

//按条件批量查找User类型的Java bean
//排序，1是升序，-1是降序
//可以设置跳过元素的数量
//可以设置限制元素的数量
List<User> users4 = db.find(User.class, u -> {
    boolean b1 = u.gender.equals("male");
    boolean b2 = u.email.contains("@gmail.com");
    return b1 && b2;
}, options -> {
    options.sort("age", 1).skip(3).limit(10);
});

//查找条件可以为null，仅设置FindOptions参数
List<User> users5 = db.find(User.class, null, options -> {
    options.sort("age", 1).skip(3).limit(10);
});



//计数：
//统计User类型数据的数量
int res1 = db.count(User.class);

//按条件统计User类型数据的数量
int res2 = db.count(User.class, u -> u.age >= 18);



//手动关闭数据库
db.close();

//销毁数据库
db.destroy();
```

### 2. 存储 K-V 类型数据
开始使用
```java
//创建QuickIO.KV对象，并设置存储目录
QuickIO.KV kv = new QuickIO.KV("sample_kv");



//保存基本类型数据，并按值推断类型。
kv.write("Int", 2022);
kv.write("Long", 1015653787903332352L);
kv.write("Float", 3.14f);
kv.write("Double", 3.141592654d);
kv.write("Bool", true);
kv.write("Char", 'c');
kv.write("String", "Hello world!");

//读取基本类型数据，并按默认值推断类型
int i = kv.read("Int", 0);
long l = kv.read("Long", 0L);
float f = kv.read("Float", 0f);
double d = kv.read("Double", 0d);
boolean b = kv.read("Bool", false);
char c = kv.read("Char", '0');
String s = kv.read("String", "");

//按指定的key移除数据，如果移除成功，则为true，否则为false
boolean b1 = kv.remove("Int");

//查询该key是否存在，如果存在，则为真，否则为假
boolean b2 = kv.containsKey("Long");



//存储Java bean
//创建对象，并使用Serializable接口
public class User implements Serializable {
    public String name;
    public Integer age;

    public User(Consumer<User> consumer) {
        consumer.accept(this);
    }
}

//保存Java bean数据
kv.write("Li Ming", new User(u -> {
    u.name = "Li Ming";
    u.age = 18;
}));

//读取Java bean数据
User user = kv.read("Li Ming", User.class);
if (user != null) {
    System.out.println(user.name + " " + user.age);
}



//手动关闭数据库
kv.close();

//销毁数据库
kv.destroy();
```


### 3. 存储文件
```java
//创建QuickIO.Can对象，并设置存储目录
QuickIO.Can can = new QuickIO.Can("sample_can");

//将文件保存到罐头中，并更改文件名
can.put("test.png", new File("..."));

//从罐头中获取指定的文件
File file = can.get("test.png");
if (file != null) {
    System.out.println(file.getPath());
}

//从罐头中移除指定的文件
can.remove("test.png");

//遍历罐头中的所有文件
List<File> files = can.list();

//循环读取罐头中的文件；若返回true，则继续循环；若返回false，则中断循环
can.foreach(file -> {
    System.out.println(file.getName());
    return true;
});

//销毁罐头
can.destroy();
```


### 4. 小工具
```java
//使用Twitter开源分布式ID生成算法（Snowflake）生成唯一ID，该ID强烈依赖于机器时钟
long id = QuickIO.id();

//通过Snowflake ID获取时间戳
long timestamp = QuickIO.toTimestamp(id);
```


## 样品
[Here](https://github.com/artbits/quickio/tree/main/src/test/java/sample)


## 感谢
QuickIO使用到的开源项目
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