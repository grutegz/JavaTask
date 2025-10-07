package app.logic;

import app.questions.Question;
import java.util.Locale;

public final class SimpleEvaluator implements Evaluator {
    @Override
    public boolean isCorrect(Question q, String userAnswer) {
        if (q == null || userAnswer == null) return false;
        String a = q.answer().trim();
        String u = userAnswer.trim();
        return a.toLowerCase(Locale.ROOT).equals(u.toLowerCase(Locale.ROOT));
    }
}
