package apis;

import com.github.artbits.quickio.api.JTin;
import com.github.artbits.quickio.core.Config;
import com.github.artbits.quickio.core.QuickIO;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

final class TinExample {

    //A static Tin object.  When the program ends running, the JVM automatically closes the object.
    private final static JTin tin = QuickIO.tin("example_tin");


    @Test
    void config() {
        Config config = Config.of(c -> {
            c.name("example_tin");
            c.path("/usr/qio");                 //Custom base path.
        });

        try (JTin tin1 = QuickIO.tin(config)) {
            //DB operation.
        }
    }


    @Test
    void apis() throws IOException {
        //Storage network file stream.
        InputStream inputStream = Files.newInputStream(new File("...").toPath());
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        inputStream.close();
        tin.put("photo.png", bytes);


        tin.put("photo.png", new File("..."));

        //Storing files through network URL
        tin.put("baidu_image.png", "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png");


        File file = tin.get("photo.png");


        tin.remove("photo.png");


        List<File> files = tin.list();


        tin.foreach(f -> {
            QuickIO.println(f.getPath());
            return true;             //True is to continue, false is to break.
        });
    }


}
