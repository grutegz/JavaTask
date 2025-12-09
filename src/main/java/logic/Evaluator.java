package logic;

import questions.Question;

public interface Evaluator {
    boolean isCorrect(Question q, String userAnswer);
}