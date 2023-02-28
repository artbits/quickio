package apis;

import com.github.artbits.quickio.api.KV;
import com.github.artbits.quickio.core.Config;
import com.github.artbits.quickio.core.QuickIO;
import org.junit.jupiter.api.Test;

final class KVExample {

    //A static KV object. When the program runs, the JVM automatically closes the object.
    private final static KV kv = QuickIO.usingKV("example_kv");


    @Test
    void config() {
        Config config = Config.of(c -> {
            c.name("example_kv");
            c.path("/usr/qio");                 //Custom base path.
            c.cache(16L * 1024 * 1024);         //Set cache size.
        });

        try(KV kv1 = QuickIO.usingKV(config)) {
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

        kv.erase("Pi");                              //Erase data.
        boolean b = kv.contains("Pi");
        QuickIO.println(b);
    }

}
