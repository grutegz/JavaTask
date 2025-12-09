package questions;

import java.util.Random;

public final class RandomQuestionSource implements QuestionSource {

    private final QuestionSource[] sources;
    private final Random rng = new Random();

    public RandomQuestionSource(QuestionSource... sources) {
        if (sources == null || sources.length == 0) {
            throw new IllegalArgumentException("Необходимо предоставить хотя бы один источник вопросов.");
        }
        this.sources = sources;
    }

    @Override
    public Question next() {
        int index = rng.nextInt(sources.length);
        return sources[index].next();
    }
}