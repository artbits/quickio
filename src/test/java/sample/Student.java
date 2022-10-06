package sample;

import com.github.artbits.quickio.QuickIO;

import java.util.function.Consumer;

final class Student extends QuickIO.Object {

    String name;
    Integer gender;
    Integer age;
    Long departmentId;
    Long scoreId;

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