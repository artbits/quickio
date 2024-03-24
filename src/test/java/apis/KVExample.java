package apis;

import com.github.artbits.quickio.api.JKV;
import com.github.artbits.quickio.core.Config;
import com.github.artbits.quickio.core.QuickIO;
import org.junit.jupiter.api.Test;

import java.util.Objects;

final class KVExample {

    //A static KV object. When the program ends running, the JVM automatically closes the object.
    private final static JKV kv = QuickIO.kv("example_kv");


    @Test
    void config() {
        Config config = Config.of(c -> {
            c.name("example_kv");
            c.path("/usr/qio");                 //Custom base path.
            c.cache(16L * 1024 * 1024);         //Set cache size.
        });

        try (JKV kv1 = QuickIO.kv(config)) {
            //DB operation.
        }
    }


    @Test
    void apis() {
        kv.set("Pi", 3.14);
        kv.set(3.14, "Pi");


        double d = kv.get("Pi", Double.class);
        String s = kv.get(3.14, String.class);
        String s1 = kv.get(3.1415, "unknown");      //The key does not exist, return the default value.
        QuickIO.println("%s = %f, s1 = %s", s, d, s1);


        //Get the type of value
        String type1 = kv.type("Pi");
        String type2 = kv.type(3.14);
        QuickIO.println("type1 = %s, type2 = %s", type1, type2);


        //Erase data.
        kv.del("Pi");
        boolean b = kv.exists("Pi");
        QuickIO.println(b);


        //Rename key
        kv.set("name", "Lisa");
        QuickIO.println("name = " + kv.get("name", String.class));
        kv.rename("name", "username");              //The old key is name, and the new key is username.
        QuickIO.println("name = %s, username = %s", kv.get("name", String.class), kv.get("username", String.class));
    }


    @Test
    void foreach_apis() {
        kv.set("username_Lark", 18);
        kv.set("username_Lisa", 22);
        kv.set("username_Amy", 25);


        // Query all keys and values of the specified type.
        kv.foreach(String.class, Integer.class, (k, v) -> {
            QuickIO.println(k + " = " + v);
        });


        // Query all keys and values of the specified type. Can be interrupted.
        kv.foreach(String.class, Integer.class, (k, v) -> {
            QuickIO.println(k + " = " + v);
            //True is to continue, false is to break.
            return !Objects.equals("username_Lark", k);
        });
    }

}
