package simple;

import com.github.artbits.quickio.IObject;

import java.util.List;
import java.util.function.Consumer;

final class Department extends IObject {

    String name;
    List<Long> studentIds;

    Department(Consumer<Department> consumer) {
        consumer.accept(this);
    }

}
