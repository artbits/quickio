package apis;

import com.github.artbits.quickio.api.Tin;
import com.github.artbits.quickio.core.Config;
import com.github.artbits.quickio.core.QuickIO;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

final class TinExample {

    //A static Tin object. When the program runs, the JVM automatically closes the object.
    private final static Tin tin = QuickIO.usingTin("example_tin");


    @Test
    void config() {
        Config config = Config.of(c -> {
            c.name("example_tin");
            c.path("/usr/qio");                 //Custom base path.
            c.cache(16L * 1024 * 1024);         //Set cache size.
        });

        try(Tin tin1 = QuickIO.usingTin(config)) {
            //DB operation.
        }
    }


    @Test
    void apis() {
        tin.put("photo.png", new File("..."));

        File file = tin.get("photo.png");

        tin.remove("photo.png");

        List<File> files = tin.list();

        tin.foreach(f -> {
            QuickIO.println(f.getPath());
            return true;             //True is to continue, false is to break.
        });
    }


}
