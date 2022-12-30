package sample;

import com.github.artbits.quickio.QuickIO;

import java.util.function.Consumer;

final class Score extends QuickIO.Object {
    Long studentId;
    Integer language;
    Integer maths;
    Integer english;

    Score(Consumer<Score> consumer) {
        consumer.accept(this);
    }
}