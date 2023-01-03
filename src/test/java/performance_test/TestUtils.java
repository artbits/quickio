package performance_test;

import com.github.artbits.quickio.QuickIO;

import java.util.function.Consumer;

class TestUtils {

    static void timer(String name, Runnable runnable) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        QuickIO.println("[%s] time consumption: %dms|%fs", name, duration, duration/1000.0);
    }


    static void foreach(long length, Consumer<Long> consumer) {
        for (long i = 0; i < length; i++) {
            consumer.accept(i);
        }
    }

}
