package app.logic;

import app.questions.Question;

public interface Evaluator {
    boolean isCorrect(Question q, String userAnswer);
}