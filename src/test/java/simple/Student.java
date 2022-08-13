package simple;

import com.github.artbits.quickio.IObject;

import java.util.function.Consumer;

final class Student extends IObject {

    String name;
    int gender;
    int age;
    long departmentId;
    long scoreId;

    interface Gender {
        int MALE = 0;
        int FEMALE = 1;
    }

    Student(Consumer<Student> consumer) {
        consumer.accept(this);
    }

    @Override
    public String toString() {
        String s = gender == Gender.MALE ? "Male" : "Female";
        return String.format("%d %s %s %d", this.id(), name, s, age);
    }

}
