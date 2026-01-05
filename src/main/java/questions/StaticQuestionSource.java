package questions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class StaticQuestionSource implements QuestionSource {
    private final List<Question> questions;
    private int index = 0;

    public StaticQuestionSource(List<Question> questions) {
        if (questions == null || questions.isEmpty()) throw new IllegalArgumentException("questions must not be empty");
        this.questions = Collections.unmodifiableList(questions);
    }

    public StaticQuestionSource(Question... questions) {
        this(Arrays.asList(questions));
    }

    @Override
    public Question next() {
        Question q = questions.get(index);
        index = (index + 1) % questions.size();
        return q;
    }
}