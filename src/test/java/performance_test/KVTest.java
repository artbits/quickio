package performance_test;

import com.github.artbits.quickio.QuickIO;
import org.junit.jupiter.api.Test;

import static performance_test.TestUtils.foreach;
import static performance_test.TestUtils.timer;

class KVTest {

    private final static long LENGTH = 100_000;
    private final static String KV_NAME = "api_basic_test_kv";


    @Test
    void write_long_type_value() {
        resetData();
        timer("write_long_type_value", () -> {
            QuickIO.KV.open(KV_NAME, kv -> {
                foreach(LENGTH, i -> kv.write(String.valueOf(i), QuickIO.id()));
            });
        });
    }


    @Test
    void write_string_type_value() {
        resetData();
        timer("write_string_type_value", () -> {
            QuickIO.KV.open(KV_NAME, kv -> {
                foreach(LENGTH, i -> kv.write(String.valueOf(i), String.valueOf(QuickIO.id())));
            });
        });
    }


    @Test
    void write_object_type_value() {
        resetData();
        timer("write_object_type_value", () -> {
            QuickIO.KV.open(KV_NAME, kv -> {
                foreach(LENGTH, i -> kv.write(String.valueOf(i), new User(u -> {
                    u.name = "Human" + i;
                    u.age = i;
                    u.gender = (i % 2 == 0) ? "male" : "female";
                    u.email = u.name + "@github.com";
                })));
            });
        });
    }


    @Test
    void read_long_type_value() {
        resetData();
        write_long_type_value();
        timer("read_long_type_value", () -> {
            QuickIO.KV.open(KV_NAME, kv -> {
               kv.read(String.valueOf(LENGTH / 2), -1L);
            });
        });
    }


    @Test
    void read_string_type_value() {
        resetData();
        write_string_type_value();
        timer("read_string_type_value", () -> {
            QuickIO.KV.open(KV_NAME, kv -> {
                kv.read(String.valueOf(LENGTH / 2), "empty");
            });
        });
    }


    @Test
    void read_object_type_value() {
        resetData();
        write_object_type_value();
        timer("read_object_type_value", () -> {
            QuickIO.KV.open(KV_NAME, kv -> {
                kv.read(String.valueOf(LENGTH / 2), User.class);
            });
        });
    }


    @Test
    void remove_key() {
        resetData();
        write_long_type_value();
        timer("remove_key", () -> {
            QuickIO.KV.open(KV_NAME, kv -> {
                kv.remove(String.valueOf(LENGTH / 2));
            });
        });
    }


    @Test
    void contains_key() {
        resetData();
        write_long_type_value();
        timer("contains_key", () -> {
            QuickIO.KV.open(KV_NAME, kv -> {
                kv.containsKey(String.valueOf(LENGTH / 2));
            });
        });
    }


    private void resetData() {
        QuickIO.KV.open(KV_NAME, kv -> kv.destroy());
    }

}
