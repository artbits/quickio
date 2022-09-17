package simple;

import com.github.artbits.quickio.QuickIO;

import java.util.List;
import java.util.function.Consumer;

final class Department extends QuickIO.Object {

    String name;
    List<Long> studentIds;

    Department(Consumer<Department> consumer) {
        consumer.accept(this);
    }

}