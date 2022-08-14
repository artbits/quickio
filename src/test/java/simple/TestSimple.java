package simple;

import com.github.artbits.quickio.QuickIO;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class TestSimple {

    static {
        QuickIO.init();
    }

    @Test
    void create_department_test() {
        Department department = new Department(d -> {
            d.name = "Computer science";
            d.studentIds = new ArrayList<>();
        });
        QuickIO.save(department);

        System.out.println(department.id());
        System.out.println(department.timestamp());
    }

    @Test
    void save_student_test() {
        Department department = QuickIO.findOne(Department.class, d -> "Computer science".equals(d.name));
        if (department == null) {
            System.out.println("department object is null.");
            return;
        }

        Student student = new Student(s -> {
            s.name = "Li Ming";
            s.age = 18;
            s.gender = Student.Gender.MALE;
            s.departmentId = department.id();
        });
        QuickIO.save(student);

        department.studentIds.add(student.id());
        QuickIO.save(department);

        System.out.println(student);
    }

    @Test
    void save_students_test() {
        Department department = QuickIO.findOne(Department.class, d -> "Computer science".equals(d.name));
        if (department == null) {
            System.out.println("department object is null.");
            return;
        }

        List<Student> students = new ArrayList<>();
        students.add(new Student(s -> {
            s.name = "Li Hua";
            s.age = 22;
            s.gender = Student.Gender.MALE;
            s.departmentId = department.id();
        }));
        students.add(new Student(s -> {
            s.name = "Lisa";
            s.age = 19;
            s.gender = Student.Gender.MALE;
            s.departmentId = department.id();
        }));
        students.add(new Student(s -> {
            s.name = "Amy";
            s.age = 20;
            s.gender = Student.Gender.MALE;
            s.departmentId = department.id();
        }));
        QuickIO.save(students);

        students.forEach(student -> department.studentIds.add(student.id()));
        QuickIO.save(department);
    }

    @Test
    void save_score_test() {
        Random random = new Random();
        List<Student> students = QuickIO.find(Student.class);
        students.forEach(student -> {
            Score score = new Score(s -> {
                s.studentId = student.id();
                s.language = random.nextInt(100);
                s.english = random.nextInt(100);
                s.maths = random.nextInt(100);
            });
            QuickIO.save(score);
            student.scoreId = score.id();
        });
        QuickIO.save(students);
    }

    @Test
    void update_students_data_test() {
        Student student = new Student(s -> s.gender = Student.Gender.FEMALE);
        QuickIO.update(student, s -> {
            boolean b1 = "Lisa".equals(s.name);
            boolean b2 = "Amy".equals(s.name);
            return b1 || b2;
        });
    }

    @Test
    void find_all_students_test() {
        List<Student> students = QuickIO.find(Student.class);
        students.forEach(System.out::println);
    }

    @Test
    void find_students_by_department_test() {
        Department department = QuickIO.findOne(Department.class, d -> "Computer science".equals(d.name));
        if (department == null) {
            System.out.println("department object is null.");
            return;
        }

        int index = department.studentIds.size();
        long[] studentIds = new long[index];
        for (int i = 0; i < index; i++) {
            studentIds[i] = department.studentIds.get(i);
        }

        List<Student> students = QuickIO.find(Student.class, studentIds);
        students.forEach(System.out::println);
    }

    @Test
    void find_first_student_test() {
        Student student = QuickIO.findFirst(Student.class);
        System.out.println(student);
    }

    @Test
    void find_last_student_test() {
        Student student = QuickIO.findLast(Student.class);
        System.out.println(student);
    }

    @Test
    void find_all_students_score_test() {
        List<Student> students = QuickIO.find(Student.class);
        students.forEach(student -> {
            Score score = QuickIO.find(Score.class, student.scoreId);
            System.out.println(student.name + "   " + score);
        });
    }

    @Test
    void find_students_english_score_above_60_test() {
        List<Score> scores = QuickIO.find(Score.class, s -> s.english >= 60);
        scores.forEach(score -> {
            Student student = QuickIO.find(Student.class, score.studentId);
            System.out.println(student.name + "   " + score.maths);
        });
    }

    @Test
    void sort_students_maths_score_test() {
        List<Score> scores = QuickIO.find(Score.class, null, options -> options.sort("maths", -1));
        scores.forEach(score -> {
            Student student = QuickIO.find(Student.class, score.studentId);
            System.out.println(student.name + "   " + score.maths);
        });
    }

    @Test
    void find_student_highest_score_maths_test() {
        List<Score> scores = QuickIO.find(Score.class, null, options -> {
            options.sort("maths", -1);
            options.limit(1);
        });
        if (scores.size() == 1) {
            Score score = scores.get(0);
            Student student = QuickIO.find(Student.class, score.studentId);
            System.out.println(student.name + "   " + score.maths);
        }
    }

    @Test
    void delete_department_by_id_test() {
        Department department = new Department(d -> {
            d.name = "unknown";
            d.studentIds = new ArrayList<>();
        });
        QuickIO.save(department);
        System.out.println(department.id());

        boolean b = QuickIO.delete(department.id());
        System.out.println(b);

        QuickIO.find(Department.class).forEach(d -> System.out.println(d.name));
    }

    @Test
    void delete_departments_by_ids_test() {
        List<Department> departments = new ArrayList<>();
        departments.add(new Department(d -> {
            d.name = "unknown";
            d.studentIds = new ArrayList<>();
        }));
        departments.add(new Department(d -> {
            d.name = "unknown";
            d.studentIds = new ArrayList<>();
        }));
        QuickIO.save(departments);

        int index = departments.size();
        long[] departmentIds = new long[index];
        for (int i = 0; i < index; i++) {
            departmentIds[i] = departments.get(i).id();
        }

        QuickIO.delete(departmentIds);

        QuickIO.find(Department.class).forEach(d -> System.out.println(d.name));
    }

    @Test
    void delete_departments_by_condition_test() {
        Department department = new Department(d -> {
            d.name = "unknown";
            d.studentIds = new ArrayList<>();
        });
        QuickIO.save(department);
        QuickIO.delete(Department.class, d -> "unknown".equals(d.name));
        QuickIO.find(Department.class).forEach(d -> System.out.println(d.name));
    }

    @Test
    void delete_all_departments_test() {
        QuickIO.delete(Department.class);
        QuickIO.find(Department.class).forEach(d -> System.out.println(d.name));
    }

    @Test
    void init_test() {
        QuickIO.init("dir");
    }

    @Test
    void destroy_test() {
        try {
            QuickIO.destroy();
            List<Student> students = QuickIO.find(Student.class);
            students.forEach(System.out::println);
        } catch (NullPointerException e) {
            System.out.println("QuickIO destroyed");
        }
    }

}
