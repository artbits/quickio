package simple;

import com.github.artbits.quickio.IObject;

import java.util.function.Consumer;

final class Score extends IObject {

    long studentId;
    int language;
    int maths;
    int english;

    Score(Consumer<Score> consumer) {
        consumer.accept(this);
    }

    @Override
    public String toString() {
        return String.format("language = %d  maths = %d  english = %d", language, maths, english);
    }
}
