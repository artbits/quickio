package performance_test;

import com.github.artbits.quickio.QuickIO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static performance_test.TestUtils.foreach;
import static performance_test.TestUtils.timer;

class DBTest {

    private final static long LENGTH = 100_000;
    private final static String DB_NAME = "api_basic_test_db";


    @Test
    void alone_save() {
        resetData();
        timer("alone_save", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                foreach(LENGTH, i -> {
                    db.save(new User(u -> {
                        u.name = "Human" + i;
                        u.age = i;
                        u.gender = (i % 2 == 0) ? "male" : "female";
                        u.email = u.name + "@github.com";
                    }));
                });
            });
        });
    }


    @Test
    void batch_save() {
        resetData();
        timer("batch_save", () -> {
            List<User> users = new ArrayList<>();
            foreach(LENGTH, i -> users.add(new User(u -> {
                u.name = "Human" + i;
                u.age = i;
                u.gender = (i % 2 == 0) ? "male" : "female";
                u.email = u.name + "@github.com";
            })));
            QuickIO.DB.open(DB_NAME, db -> db.save(users));
        });
    }


    @Test
    void alone_update() {
        resetData();
        initData();
        List<User> users = QuickIO.DB.openGet(DB_NAME, db -> db.find(User.class));
        timer("alone_update", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                users.forEach(u -> {
                    u.gender = "male".equals(u.gender) ? "female" : "male";
                    db.save(u);
                });
            });
        });
    }


    @Test
    void batch_update() {
        resetData();
        initData();
        timer("batch_update", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.update(new User(u -> u.gender = "female"), u -> "male".equals(u.gender));
                db.update(new User(u -> u.gender = "male"), u -> "female".equals(u.gender));
            });
        });
    }


    @Test
    void find_first() {
        resetData();
        initData();
        timer("find_first", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findFirst(User.class);
            });
        });
    }


    @Test
    void find_first_by_condition() {
        resetData();
        initData();
        timer("find_first_by_condition", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findFirst(User.class, u -> u.age == (LENGTH / 2));
            });
        });
    }


    @Test
    void find_last() {
        resetData();
        initData();
        timer("find_last", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findLast(User.class);
            });
        });
    }


    @Test
    void find_last_by_condition() {
        resetData();
        initData();
        timer("find_last_by_condition", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findLast(User.class, u -> u.age == (LENGTH / 2));
            });
        });
    }


    @Test
    void find_one_by_id() {
        resetData();
        initData();
        long id = QuickIO.DB.openGet(DB_NAME, db -> db.findOne(User.class, u -> u.age == (LENGTH / 2)).id());
        timer("find_one_by_id", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.find(User.class, id);
            });
        });
    }


    @Test
    void find_one_by_condition() {
        resetData();
        initData();
        timer("find_one_by_condition", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findOne(User.class, u -> u.age == (LENGTH / 2));
            });
        });
    }


    @Test
    void find_all() {
        resetData();
        initData();
        timer("find_all", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.find(User.class);
            });
        });
    }


    @Test
    void find_all_by_ids() {
        resetData();
        initData();
        List<Long> ids = QuickIO.DB.openGet(DB_NAME, db -> db.find(User.class)
                .stream()
                .map(QuickIO.Object::id)
                .collect(Collectors.toList()));
        timer("find_all_by_ids", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.find(User.class, ids);
            });
        });
    }


    @Test
    void find_by_conditions() {
        resetData();
        initData();
        timer("find_by_conditions", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.find(User.class, u -> {
                    boolean b1 = "male".equals(u.gender);
                    boolean b2 = u.email.contains("Human" + u.age % 3);
                    return b1 && b2;
                });
            });
        });
    }


    @Test
    void find_by_conditions_options_sort() {
        resetData();
        initData();
        timer("find_by_conditions_options_sort", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.find(User.class, u -> {
                    boolean b1 = "male".equals(u.gender);
                    boolean b2 = u.email.contains("Human" + u.age % 3);
                    return b1 && b2;
                }, options -> options.sort("age", 1));
            });
        });
    }


    @Test
    void find_by_conditions_options_sort_skip() {
        resetData();
        initData();
        timer("find_by_conditions_options_sort_skip", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.find(User.class, u -> {
                    boolean b1 = "male".equals(u.gender);
                    boolean b2 = u.email.contains("Human" + u.age % 3);
                    return b1 && b2;
                }, options -> options.sort("age", 1).skip(LENGTH / 1000));
            });
        });
    }


    @Test
    void find_by_conditions_options_sort_skip_limit() {
        resetData();
        initData();
        timer("find_by_conditions_options_sort_skip_limit", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.find(User.class, u -> {
                    boolean b1 = "male".equals(u.gender);
                    boolean b2 = u.email.contains("Human" + u.age % 3);
                    return b1 && b2;
                }, options -> options.sort("age", 1).skip(LENGTH / 1000).limit(LENGTH / 1000));
            });
        });
    }


    @Test
    void find_with_id() {
        resetData();
        initData();
        long uid = QuickIO.DB.openGet(DB_NAME, db -> db.findOne(User.class, u -> u.age == LENGTH / 2).id());
        timer("find_with_id", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findWithID(User.class, id -> id >= uid);
            });
        });
    }


    @Test
    void find_with_id_options_sort() {
        resetData();
        initData();
        long uid = QuickIO.DB.openGet(DB_NAME, db -> db.findOne(User.class, u -> u.age == LENGTH / 2).id());
        timer("find_with_id_options_sort", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findWithID(User.class, id -> id >= uid, options -> options.sort("age", -1));
            });
        });
    }


    @Test
    void find_with_id_options_sort_skip() {
        resetData();
        initData();
        long uid = QuickIO.DB.openGet(DB_NAME, db -> db.findOne(User.class, u -> u.age == LENGTH / 2).id());
        timer("find_with_id_options_sort_skip", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findWithID(User.class, id -> id >= uid, options -> {
                    options.sort("age", -1).skip(LENGTH / 10);
                });
            });
        });
    }


    @Test
    void find_with_id_options_sort_skip_limit() {
        resetData();
        initData();
        long uid = QuickIO.DB.openGet(DB_NAME, db -> db.findOne(User.class, u -> u.age == LENGTH / 2).id());
        timer("find_with_id_options_sort_skip_limit", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findWithID(User.class, id -> id >= uid, options -> {
                    options.sort("age", -1).skip(LENGTH / 10).limit(LENGTH / 10);
                });
            });
        });
    }


    @Test
    void find_with_time() {
        resetData();
        initData();
        long time = QuickIO.DB.openGet(DB_NAME, db -> db.findOne(User.class, u -> u.age == LENGTH / 2).timestamp());
        timer("find_with_time", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findWithTime(User.class, timestamp -> timestamp >= time);
            });
        });
    }


    @Test
    void find_with_time_options_sort() {
        resetData();
        initData();
        long time = QuickIO.DB.openGet(DB_NAME, db -> db.findOne(User.class, u -> u.age == LENGTH / 2).timestamp());
        timer("find_with_time_options_sort", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findWithTime(User.class, timestamp -> timestamp >= time, options -> {
                    options.sort("age", -1);
                });
            });
        });
    }


    @Test
    void find_with_time_options_sort_skip() {
        resetData();
        initData();
        long time = QuickIO.DB.openGet(DB_NAME, db -> db.findOne(User.class, u -> u.age == LENGTH / 2).timestamp());
        timer("find_with_time_options_sort_skip", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findWithTime(User.class, timestamp -> timestamp >= time, options -> {
                    options.sort("age", -1).skip(LENGTH / 10);
                });
            });
        });
    }


    @Test
    void find_with_time_options_sort_skip_limit() {
        resetData();
        initData();
        long time = QuickIO.DB.openGet(DB_NAME, db -> db.findOne(User.class, u -> u.age == LENGTH / 2).timestamp());
        timer("find_with_time_options_sort_skip_limit", () -> {
            QuickIO.DB.open(DB_NAME, db -> {
                db.findWithTime(User.class, timestamp -> timestamp >= time, options -> {
                    options.sort("age", -1).skip(LENGTH / 10).limit(LENGTH / 10);
                });
            });
        });
    }


    @Test
    void along_delete() {
        resetData();
        initData();
        List<Long> ids = QuickIO.DB.openGet(DB_NAME, db -> db.find(User.class)
                .stream()
                .map(QuickIO.Object::id)
                .collect(Collectors.toList()));
        timer("along_delete", () -> {
            QuickIO.DB.open(DB_NAME, db -> ids.forEach(db::delete));
        });
    }


    @Test
    void batch_delete() {
        resetData();
        initData();
        List<Long> ids = QuickIO.DB.openGet(DB_NAME, db -> db.find(User.class)
                        .stream()
                        .map(QuickIO.Object::id)
                        .collect(Collectors.toList()));
        timer("batch_delete", () -> {
            QuickIO.DB.open(DB_NAME, db -> db.delete(ids));
        });
    }


    @Test
    void delete_all_by_class() {
        resetData();
        initData();
        timer("delete_all_by_class", () -> {
            QuickIO.DB.open(DB_NAME, db -> db.delete(User.class));
        });
    }


    @Test
    void delete_by_condition() {
        resetData();
        initData();
        timer("delete_by_condition", () -> {
            QuickIO.DB.open(DB_NAME, db -> db.delete(User.class, u -> "male".equals(u.gender)));
        });
    }


    private void initData() {
        List<User> users = new ArrayList<>();
        foreach(LENGTH, i -> users.add(new User(u -> {
            u.name = "Human" + i;
            u.age = i;
            u.gender = (i % 2 == 0) ? "male" : "female";
            u.email = u.name + "@github.com";
        })));
        QuickIO.DB.open(DB_NAME, db -> db.save(users));
    }


    private void resetData() {
        QuickIO.DB.open(DB_NAME, db -> db.destroy());
    }

}
