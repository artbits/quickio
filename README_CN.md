[![](https://www.jitpack.io/v/artbits/quickio.svg)](https://www.jitpack.io/#artbits/quickio)
[![](https://img.shields.io/badge/JDK-%3E%3D%208-orange)](https://jdk.java.net/)
[![](https://img.shields.io/badge/license-Apache--2.0-blue)](#license)

[English](README.md) | 中文

# QuickIO
QuickIO 是一个多功能嵌入式数据库。底层基于 LevelDB 引擎和 Java NIO 设计，并使用 Hessian 序列化/反序列化数据。支持存储 Java bean、Key-Value 格式和文件类型的数据。零配置，使用 Java 代码操作，快速高效。

+ 优点
   + 像 ``SQLite`` 一样的嵌入式数据库，不需要安装和配置。
   + 像 ``MongoDB`` 或 ``Diskv`` 一样的NoSQL数据库，使用简单。
   + 基于 ``Leveldb`` 设计的 **唯一索引** ，非常高效。
   + 支持存储Java bean、Key-Value格式和文件类型的数据。
   + 简易的API，使用Java Lambda表达式优雅操作。
   + 读写快速，满足中小型数据量的使用场景。
+ 缺点
   + 非关系型数据库，不支持SQL语句和事务。
   + 只支持单进程运行，不支持多进程。
+ 了解更多
   + 🚀 了解 QuickIO 性能数据，请点击 [这里](performance_data.md)。
   + 🎯 了解作者使用 QuickIO 编写的 RSS 服务器程序：[RSS-Svr](https://github.com/artbits/rss-svr)


## 下载
Gradle:
```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.artbits:quickio:1.2.3'
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
    <version>1.2.3</version>
</dependency>
```


## 如何使用QuickIO？

### 1. 存储Java bean
```java
//创建一个Java bean，并继承 QuickIO.Object 类
public class User extends QuickIO.Object {
    public Integer age;
    public String name;
    public String gender;
    @Index                  //唯一索引注解，可选择性添加
    public String email;

    public User(Consumer<User> consumer) {
        consumer.accept(this);
    }
}



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
QuickIO.println(user.id());
//若保存成功，则可获取到保存时的时间戳
QuickIO.println(user.timestamp());
//Java bean转JSON
QuickIO.println(user.toJson());

//通过ID更新已存储的数据
user.age = 20;
db.save(user);

//批量保存数据
List<User> users = Arrays.asList(user1, user2, user3);
db.save(users);
//Java bean以JSON格式打印到操控台
users.forEach(QuickIO.Object::printJson);



//更新：
//新建User对象，并设置要修改的数据
User user = new User(u -> u.age = 25);

//按条件更新数据
db.update(user, u -> {
    boolean b1 = Objects.equals(u.name, "LiMing");
    boolean b2 = Objects.equals(u.email, "liming@foxmail.com");
    return b1 && b2;
});



//删除：
//通过ID删除；若删除成功，则返回值为true
boolean res = db.delete(user.id());
QuickIO.println(res);

//通过ID批量删除
db.delete(id1, id2, id3, id4);

//通过ID list批量删除
db.delete(Arrays.asList(id1, id2, id3, id4));

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

//按ID列表批量查找User类型的Java bean
List<User> users3 = db.find(User.class, Arrays.asList(id1, id2, id3, id4));

//按条件批量查找User类型的Java bean
List<User> users4 = db.find(User.class, u -> u.age >= 18);

//按条件批量查找User类型的Java bean
//排序，1是升序，-1是降序
//可以设置跳过元素的数量
//可以设置限制元素的数量
List<User> users5 = db.find(User.class, u -> {
    boolean b1 = u.gender.equals("male");
    boolean b2 = u.email.contains("@gmail.com");
    return b1 && b2;
}, options -> {
    options.sort("age", 1).skip(3).limit(10);
});

//查找条件可以为null，仅设置FindOptions参数
List<User> users6 = db.find(User.class, null, options -> {
    options.sort("age", 1).skip(3).limit(10);
});

//按ID条件查找，findWithID方法比find方法更合适
//不推荐：db.find(User.class, u -> u.id() > 1058754025064759296L);
List<User> users7 = db.findWithID(User.class, id -> id > 1058754025064759296L);

//按ID条件查找，并设置FindOptions参数
List<User> users8 = db.findWithID(User.class, id -> id > 1058754025064759296L, options -> {
    options.sort("age", 1).skip(3).limit(10);
});

//按时间戳条件查找，findWithTime方法比find方法更合适
//不推荐：db.find(User.class, u -> u.timestamp() < System.currentTimeMillis());
List<User> users9 = db.findWithTime(User.class, timestamp -> timestamp < System.currentTimeMillis());

//按时间戳条件查找，并设置FindOptions参数
List<User> users10 = db.findWithTime(User.class, timestamp -> {
    boolean b1 = QuickIO.toTimestamp(1058754025064759296L) < timestamp;
    boolean b2 = timestamp < System.currentTimeMillis();
    return b1 && b2;
}, options -> {
    options.sort("age", 1).skip(3).limit(10);
});



//索引操作：
//按索引指定查找User类型的Java bean
User user = db.findWithIndex(User.class, options -> options.index("email", "liming@gmail.com"));

//使用索引查询Java bean是否存在
boolean b = db.exist(User.class, options -> options.index("email", "liming@gmail.com"));

//删除 @Index 注解，亦需要使用 dropIndex 方法移除对应索引字段的数据
db.dropIndex(User.class, "email");



//计数：
//统计User类型数据的数量
int res1 = db.count(User.class);

//按条件统计User类型数据的数量
int res2 = db.count(User.class, u -> u.age >= 18);



//一次性打开：
//等价于Try-with-catch自动关闭
QuickIO.DB.open("sample_db", db -> {
    //Operation db.
}, e -> {
    //Exception handling.
});

//打开 -> 返回数据 -> 关闭
User user = QuickIO.DB.openGet("sample_db", db -> {
    return db.findFirst(User.class);
}, e -> {
    //Exception handling.
});



//Try-with-catch自动关闭
try (QuickIO.DB db = new QuickIO.DB("sample_db")) {
    //do something
} catch (Exception e) {
    e.printStackTrace();
}

//导出db数据
db.export(s -> {
    QuickIO.println("Path to export file: " + s);
}, e -> {
    QuickIO.println("Exception message: " + e.getMessage());
});

//手动关闭数据库，你可以将其留给JVM，而无需手动关闭它
db.close();

//销毁数据库
db.destroy();
```

### 2. 存储 K-V 类型数据
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
    QuickIO.println(user.name + " " + user.age);
}



//一次性打开：
//等价于try-with-catch自动关闭
QuickIO.KV.open("sample_kv", kv -> {
    //Operation kv.
}, e -> {
    //Exception handling.
});

//打开 -> 返回数据 -> 关闭
boolean b = QuickIO.KV.openGet("sample_kv", kv -> {
    return kv.read("Bool", false);
}, e -> {
    //Exception handling.
});



//Try-with-catch自动关闭
try (QuickIO.KV kv = new QuickIO.KV("sample_kv")) {
    //do something
} catch (Exception e) {
    e.printStackTrace();
}

//导出kv数据
kv.export(s -> {
    QuickIO.println("Path to export file: " + s);
}, e -> {
    QuickIO.println("Exception message: " + e.getMessage());
});

//手动关闭数据库，你可以将其留给JVM，而无需手动关闭它
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
    QuickIO.println(file.getPath());
}

//从罐头中移除指定的文件
can.remove("test.png");

//遍历罐头中的所有文件
List<File> files = can.list();

//循环读取罐头中的文件；若返回true，则继续循环；若返回false，则中断循环
can.foreach(file -> {
    QuickIO.println(file.getName());
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

//Java bean转JSON
String json = QuickIO.toJson(new User(u -> {
    u.name = "LiMing";
    u.age = 18;
    u.gender = "male";
    u.email = "liming@gmail.com";
}));

//Java bean以JSON格式直接打印到操控台
QuickIO.printJson(new User(u -> {
    u.name = "LiMing";
    u.age = 18;
    u.gender = "male";
    u.email = "liming@gmail.com";
}));

//打印数据到操控台的方法
QuickIO.print("Hello world\n");
QuickIO.print("%d %f %c %s\n", 1, 3.14f, 'c', "Hello world");
QuickIO.println("Hello world");
QuickIO.println("%d %f %c %s", 1, 3.14f, 'c', "Hello world");
```


### 5. 小提示
```java
//提示一：
//自定义DB、KV和Can的参数
//自定义DB参数
QuickIO.DB db = new QuickIO.DB(options -> options
        .name("sample_db")              //DB名称
        .basePath("/usr/qio")           //自定义DB存储的基础目录路径
        .cacheSize(16L * 1024 *1024));  //自定义DB缓存大小，16MB

//自定义KV参数
QuickIO.KV kv = new QuickIO.KV(options -> options
        .name("sample_kv")              //KV名称
        .basePath("/usr/qio")           //自定义KV存储的基础目录路径
        .cacheSize(16L * 1024 *1024));  //自定义KV缓存大小，16MB

//自定义Can参数
QuickIO.Can can = new QuickIO.Can(options -> options
        .name("sample_can")              //Can名称
        .basePath("/usr/qio"));          //自定义Can存储的基础目录路径



//提示二：
//共享DB和独立DB的操作，KV和Can操作类似
//创建共享和独立DB
QuickIO.DB sharedDB = new QuickIO.DB("shared_db");
QuickIO.DB userDB = new QuickIO.DB("user_db");
QuickIO.DB bookDB = new QuickIO.DB("book_db");

//共享DB操作
sharedDB.save(new User("Lake", "lake@foxmail.com"));
sharedDB.save(new Book("C++ Primer Plus", "Stephen Prata"));

//独立DB操作
userDB.save(new User("Lake", "lake@foxmail.com"));
bookDB.save(new Book("C++ Primer Plus", "Stephen Prata"));



//提示三:
//查找数据时的性能优化
//假设列表包含大量元素
List<String> nameList = Arrays.asList("LiMing", "LiHua", "Lake", "Lisa");

//不推荐：
List<User> users1 = new ArrayList<>();
for (String name : nameList) {
    User user = db.findOne(User.class, u -> name.equals(u.name));
    if (user != null) {
        users1.add(user);
    }
}

//推荐：
Map<String, Boolean> map = nameList.stream().collect(Collectors.toMap(s -> s, b -> true));
List<User> users2 = db.find(User.class, u -> map.getOrDefault(u.name, false));
```


## 样品
[Here](https://github.com/artbits/quickio/tree/main/src/test/java/sample)


## 感谢
QuickIO使用到的开源项目
+ [LevelDB](https://github.com/dain/leveldb)
+ [Hessian](http://hessian.caucho.com/)


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