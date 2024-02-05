package apis;

import com.github.artbits.quickio.annotations.Index;
import com.github.artbits.quickio.api.Collection;
import com.github.artbits.quickio.api.DB;
import com.github.artbits.quickio.core.Config;
import com.github.artbits.quickio.core.IOEntity;
import com.github.artbits.quickio.core.QuickIO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

final class DBExample {

    //A static DB object.  When the program ends running, the JVM automatically closes the object.
    private final static DB db = QuickIO.usingDB("example_db");


    //Custom Entity Class.
    static class Book extends IOEntity {
        @Index
        public String isbn;         //Unique index
        public String name;
        public String author;
        public Double price;

        public static Book of(Consumer<Book> consumer) {
            Book book = new Book();
            consumer.accept(book);
            return book;
        }
    }


    @Test
    void config() {
        Config config = Config.of(c -> {
            c.name("example_db");
            c.path("/usr/qio");                 //Custom base path.
            c.cache(16L * 1024 * 1024);         //Set cache size.
        });

        try (DB db1 = QuickIO.usingDB(config)) {
            //DB operation.
        }
    }


    @Test
    void save() {
        Collection<Book> collection = db.collection(Book.class);

        collection.save(Book.of(b -> {
            b.isbn = "9787115585011";
            b.name = "On Java 8";
            b.author = "Bruce Eckel";
            b.price = 129.8;
        }));

        List<Book> books = Arrays.asList(Book.of(b -> {
            b.isbn = "9787115279460";
            b.name = "C++ Primer Plus";
            b.author = "Stephen Prata";
            b.price = 118.0;
        }), Book.of(b -> {
            b.isbn = "9787111350217";
            b.name = "Thinking in C++";
            b.author = "Bruce Eckel";
            b.price = 116.0;
        }));
        collection.save(books);
    }


    @Test
    void find() {
        Collection<Book> collection = db.collection(Book.class);

        Book book1 = collection.findFirst();
        Book book2 = collection.findFirst(b -> "Stephen Prata".equals(b.author));
        Book book3 = collection.findLast();
        Book book4 = collection.findLast(b -> "Bruce Eckel".equals(b.author));
        Book book5 = collection.findOne(b -> "On Java 8".equals(b.name));
        Book book6 = collection.findOne(book5.objectId());

        List<Book> books1 = collection.findAll();
        List<Book> books2 = collection.find(b -> "Bruce Eckel".equals(b.author));
        List<Book> books3 = collection.find(b -> b.price >= 100, options -> options.sort("price", -1).skip(0).limit(10));
        List<Book> books4 = collection.find(book1.objectId(), book3.objectId());
        List<Book> books5 = collection.find(Arrays.asList(book1.objectId(), book3.objectId()));

        List<Book> books6 = collection.findWithID(id -> id > book1.objectId());
        List<Book> books7 = collection.findWithID(id -> id > book1.objectId(), options -> options.sort("price", 1));
        List<Book> books8 = collection.findWithTime(createdAt -> createdAt < System.currentTimeMillis());
        List<Book> books9 = collection.findWithTime(createdAt -> createdAt < System.currentTimeMillis(), options -> options.sort("price", -1));
    }


    @Test
    void index() {
        Collection<Book> collection = db.collection(Book.class);

        boolean bool = collection.exist(options -> options.index("isbn", "9787115585011"));

        Book book = collection.findWithIndex(options -> options.index("isbn", "9787115585011"));

        collection.updateWithIndex(Book.of(b -> b.price = 159.0), options -> options.index("isbn", "9787115585011"));

        collection.deleteWithIndex(options -> options.index("isbn", "9787115585011"));

        //Before removing the index, delete the annotation of the entity class, and then call the method.
        collection.dropIndex("isbn");
    }


    @Test
    void update() {
        Collection<Book> collection = db.collection(Book.class);

        Book book1 = collection.findFirst();
        book1.printJson();
        book1.price *= 0.9;
        collection.save(book1);
        book1 = collection.findFirst();
        book1.printJson();

        collection.update(Book.of(b -> b.price = 129.8), b -> "9787115585011".equals(b.isbn));
        Book book2 = collection.findFirst();
        book2.printJson();
    }



    @Test
    void func() {
        Collection<Book> collection = db.collection(Book.class);
        long i1 = collection.count();
        long i2 = collection.count(b -> "Bruce Eckel".equals(b.author));
        double d1 = collection.sum("price").doubleValue();
        double d2 = collection.sum("price", b -> "Bruce Eckel".equals(b.author)).doubleValue();
        double d3 = collection.average("price");
        double d4 = collection.average("price", b -> "Bruce Eckel".equals(b.author));
        double d5 = collection.max("price").doubleValue();
        double d6 = collection.max("price", b -> "Bruce Eckel".equals(b.author)).doubleValue();
        double d7 = collection.min("price").doubleValue();
        double d8 = collection.min("price", b -> "Bruce Eckel".equals(b.author)).doubleValue();
    }


    @Test
    void delete() {
        Collection<Book> collection = db.collection(Book.class);

        Book book1 = collection.findFirst();
        Book book2 = collection.findLast();

        collection.delete(book1.objectId());
        collection.delete(book1.objectId(), book2.objectId());
        collection.delete(Arrays.asList(book1.objectId(), book2.objectId()));
        collection.delete(b -> b.createdAt() < System.currentTimeMillis());
        collection.deleteAll();
    }

}
