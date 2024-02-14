package apis;

import com.github.artbits.quickio.api.KV;
import com.github.artbits.quickio.core.Config;
import com.github.artbits.quickio.core.QuickIO;
import org.junit.jupiter.api.Test;

import java.util.Objects;

final class KVExample {

    //A static KV object. When the program ends running, the JVM automatically closes the object.
    private final static KV kv = QuickIO.usingKV("example_kv");


    @Test
    void config() {
        Config config = Config.of(c -> {
            c.name("example_kv");
            c.path("/usr/qio");                 //Custom base path.
            c.cache(16L * 1024 * 1024);         //Set cache size.
        });

        try (KV kv1 = QuickIO.usingKV(config)) {
            //DB operation.
        }
    }


    @Test
    void apis() {
        kv.write("Pi", 3.14);
        kv.write(3.14, "Pi");


        double d = kv.read("Pi", Double.class);
        String s = kv.read(3.14, String.class);
        String s1 = kv.read(3.1415, "unknown");      //The key does not exist, return the default value.
        QuickIO.println("%s = %f, s1 = %s", s, d, s1);


        //Get the type of value
        String type1 = kv.type("Pi");
        String type2 = kv.type(3.14);
        QuickIO.println("type1 = %s, type2 = %s", type1, type2);


        //Erase data.
        kv.erase("Pi");
        boolean b = kv.contains("Pi");
        QuickIO.println(b);


        //Rename key
        kv.write("name", "Lisa");
        QuickIO.println("name = " + kv.read("name", String.class));
        kv.rename("name", "username");              //The old key is name, and the new key is username.
        QuickIO.println("name = %s, username = %s", kv.read("name", String.class), kv.read("username", String.class));
    }


    @Test
    void foreach_apis() {
        kv.write("username_Lark", 18);
        kv.write("username_Lisa", 22);
        kv.write("username_Amy", 25);


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
