package sample;

import com.github.artbits.quickio.QuickIO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

final class TestSample {

    QuickIO.DB db = new QuickIO.DB("school_db");

    @Test
    void create_department_test() {
        Department department = new Department(d -> {
            d.name = "Computer science";
            d.studentIds = new ArrayList<>();
        });
        db.save(department);

        System.out.println(department.id());
        System.out.println(department.timestamp());
    }

    @Test
    void save_student_test() {
        Department department = db.findOne(Department.class, d -> "Computer science".equals(d.name));
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
        db.save(student);

        department.studentIds.add(student.id());
        db.save(department);

        System.out.println(student);
    }

    @Test
    void save_students_test() {
        Department department = db.findOne(Department.class, d -> "Computer science".equals(d.name));
        if (department == null) {
            System.out.println("department object is null.");
            return;
        }

        List<Student> students = Arrays.asList(new Student(s -> {
            s.name = "Li Hua";
            s.age = 22;
            s.gender = Student.Gender.MALE;
            s.departmentId = department.id();
        }), new Student(s -> {
            s.name = "Lisa";
            s.age = 19;
            s.gender = Student.Gender.MALE;
            s.departmentId = department.id();
        }), new Student(s -> {
            s.name = "Amy";
            s.age = 20;
            s.gender = Student.Gender.MALE;
            s.departmentId = department.id();
        }));
        db.save(students);

        students.forEach(student -> department.studentIds.add(student.id()));
        db.save(department);
    }

    @Test
    void save_score_test() {
        Random random = new Random();
        List<Student> students = db.find(Student.class);
        students.forEach(student -> {
            Score score = new Score(s -> {
                s.studentId = student.id();
                s.language = random.nextInt(100);
                s.english = random.nextInt(100);
                s.maths = random.nextInt(100);
            });
            db.save(score);
            student.scoreId = score.id();
        });
        db.save(students);
    }

    @Test
    void update_students_data_test() {
        Student student = new Student(s -> s.gender = Student.Gender.FEMALE);
        db.update(student, s -> {
            boolean b1 = "Lisa".equals(s.name);
            boolean b2 = "Amy".equals(s.name);
            return b1 || b2;
        });
    }

    @Test
    void find_all_students_test() {
        List<Student> students = db.find(Student.class);
        students.forEach(System.out::println);
    }

    @Test
    void find_students_by_department_test() {
        Department department = db.findOne(Department.class, d -> "Computer science".equals(d.name));
        if (department == null) {
            System.out.println("department object is null.");
            return;
        }

        int index = department.studentIds.size();
        long[] studentIds = new long[index];
        for (int i = 0; i < index; i++) {
            studentIds[i] = department.studentIds.get(i);
        }

        List<Student> students = db.find(Student.class, studentIds);
        students.forEach(System.out::println);
    }

    @Test
    void find_first_student_test() {
        Student student = db.findFirst(Student.class);
        System.out.println(student);
    }

    @Test
    void find_last_student_test() {
        Student student = db.findLast(Student.class);
        System.out.println(student);
    }

    @Test
    void find_all_students_score_test() {
        List<Student> students = db.find(Student.class);
        students.forEach(student -> {
            Score score = db.find(Score.class, student.scoreId);
            System.out.println(student.name + "   " + score);
        });
    }

    @Test
    void find_students_english_score_above_60_test() {
        List<Score> scores = db.find(Score.class, s -> s.english >= 60);
        scores.forEach(score -> {
            Student student = db.find(Student.class, score.studentId);
            if (student != null) {
                System.out.println(student.name + "   " + score.english);
            }
        });
    }

    @Test
    void sort_students_maths_score_test() {
        List<Score> scores = db.find(Score.class, null, options -> options.sort("maths", -1));
        scores.forEach(score -> {
            Student student = db.find(Student.class, score.studentId);
            if (student != null) {
                System.out.println(student.name + "   " + score.maths);
            }
        });
    }

    @Test
    void find_student_highest_score_maths_test() {
        List<Score> scores = db.find(Score.class, null, options -> {
            options.sort("maths", -1);
            options.limit(1);
        });
        if (scores.size() == 1) {
            Score score = scores.get(0);
            Student student = db.find(Student.class, score.studentId);
            if (student != null) {
                System.out.println(student.name + "   " + score.maths);
            }
        }
    }

    @Test
    void delete_department_by_id_test() {
        Department department = new Department(d -> {
            d.name = "unknown";
            d.studentIds = new ArrayList<>();
        });
        db.save(department);
        System.out.println(department.id());

        boolean b = db.delete(department.id());
        System.out.println(b);

        db.find(Department.class).forEach(d -> System.out.println(d.name));
    }

    @Test
    void delete_departments_by_ids_test() {
        List<Department> departments = Arrays.asList(new Department(d -> {
            d.name = "unknown";
            d.studentIds = new ArrayList<>();
        }), new Department(d -> {
            d.name = "unknown";
            d.studentIds = new ArrayList<>();
        }));
        db.save(departments);

        int index = departments.size();
        long[] departmentIds = new long[index];
        for (int i = 0; i < index; i++) {
            departmentIds[i] = departments.get(i).id();
        }
        db.delete(departmentIds);
        db.find(Department.class).forEach(d -> System.out.println(d.name));
    }

    @Test
    void delete_departments_by_list_test() {
        List<Department> departments = Arrays.asList(new Department(d -> {
            d.name = "unknown";
            d.studentIds = new ArrayList<>();
        }), new Department(d -> {
            d.name = "unknown";
            d.studentIds = new ArrayList<>();
        }));
        db.save(departments);
        db.delete(departments);
        db.find(Department.class).forEach(d -> System.out.println(d.name));
    }

    @Test
    void delete_departments_by_condition_test() {
        Department department = new Department(d -> {
            d.name = "unknown";
            d.studentIds = new ArrayList<>();
        });
        db.save(department);
        db.delete(Department.class, d -> "unknown".equals(d.name));
        db.find(Department.class).forEach(d -> System.out.println(d.name));
    }

    @Test
    void delete_all_departments_test() {
        db.delete(Department.class);
        db.find(Department.class).forEach(d -> System.out.println(d.name));
    }

    @Test
    void close_test() {
        try {
            db.close();
            List<Student> students = db.find(Student.class);
            students.forEach(System.out::println);
        } catch (NullPointerException e) {
            System.out.println("DB is closed");
        }
    }

    @Test
    void destroy_test() {
        try {
            db.destroy();
            List<Student> students = db.find(Student.class);
            students.forEach(System.out::println);
        } catch (NullPointerException e) {
            System.out.println("DB destroyed");
        }
    }

}