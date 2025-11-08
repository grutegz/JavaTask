package questions;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class FileQuestionSource implements QuestionSource {

    private final List<Question> questions;
    private int index = 0;

    public FileQuestionSource(Path path, String delimiter) {
        try {
            questions = Files.lines(path)
                    .map(l -> l.split(delimiter, 2))
                    .map(p -> new Question(p[0], p[1]))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
        if (questions.isEmpty())
            throw new IllegalArgumentException("No valid questions in file: " + path);
    }

    @Override
    public Question next() {
        Question q = questions.get(index);
        index = (index + 1) % questions.size();
        return q;
    }
}