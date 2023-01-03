package performance_test;

import com.github.artbits.quickio.QuickIO;

import java.util.function.Consumer;

class User extends QuickIO.Object {
    Long age;
    String name;
    String gender;
    String email;

    public User(Consumer<User> consumer) {
        consumer.accept(this);
    }
}
