package performance_test;

import com.github.artbits.quickio.QuickIO;
import org.junit.jupiter.api.Test;

import java.io.File;

import static performance_test.TestUtils.timer;

public class CanTest {

    private final static QuickIO.Can can = new QuickIO.Can("api_basic_test_can");


    @Test
    void put_file() {
        timer("put_file", () -> {
            can.put(System.currentTimeMillis() + ".zip", new File("test_file.zip"));
        });
    }

    @Test
    void get_file() {
        String key = System.currentTimeMillis() + ".zip";
        can.put(key, new File("test_file.zip"));
        timer("get_file", () -> {
            File file = can.get(key);
            QuickIO.println(file.length());
        });
    }

    @Test
    void remove_file() {
        String key = System.currentTimeMillis() + ".zip";
        can.put(key, new File("test_file.zip"));
        timer("remove_file", () -> {
            can.remove(key);
        });
    }

}
