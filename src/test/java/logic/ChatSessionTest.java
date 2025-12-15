package logic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import questions.Question;
import questions.QuestionSource;
import questions.StaticQuestionSource;

import static org.junit.jupiter.api.Assertions.*;

public class ChatSessionTest {

    private final Evaluator evaluator = new SimpleEvaluator();
    private final Question Q1 = new Question("Вопрос 1", "Ответ 1");
    private final Question Q2 = new Question("Вопрос 2", "Ответ 2");

    @Test
    @DisplayName("[REGULAR] При правильном ответе возвращает 'Правильно!'")
    void regularMode_handlesCorrectAnswer() {
        QuestionSource source = new StaticQuestionSource(Q1);
        ChatSession session = new ChatSession(source, evaluator, ChatSession.GameMode.REGULAR);
        session.nextQuestion();

        EvaluationResult result = session.evaluate("Ответ 1");

        assertTrue(result.isCorrect());
        assertEquals("Правильно!", result.getMessage());
    }

    @Test
    @DisplayName("[REGULAR] При неправильном ответе показывает правильный ответ")
    void regularMode_handlesIncorrectAnswer() {
        QuestionSource source = new StaticQuestionSource(Q1);
        ChatSession session = new ChatSession(source, evaluator, ChatSession.GameMode.REGULAR);
        session.nextQuestion();

        EvaluationResult result = session.evaluate("неверный ответ");

        assertFalse(result.isCorrect());
        assertTrue(result.getMessage().contains("Верный ответ: Ответ 1"));
    }

    @Test
    @DisplayName("[TIMED_TEST] При неправильном ответе НЕ показывает правильный ответ")
    void timedTest_handlesIncorrectAnswer() {
        QuestionSource source = new StaticQuestionSource(Q1);
        ChatSession session = new ChatSession(source, evaluator, 1);
        session.nextQuestion();

        EvaluationResult result = session.evaluate("неверный ответ");

        assertFalse(result.isCorrect());
        assertEquals("Неправильно.", result.getMessage());
    }

    @Test
    @DisplayName("[TIMED_TEST] Завершается ровно после ЗАДАНИЯ N-го вопроса")
    void timedTest_finishesAfterNthQuestionIsAsked() {
        final int TOTAL_QUESTIONS = 10;
        Question[] questions = new Question[TOTAL_QUESTIONS];
        for (int i = 0; i < TOTAL_QUESTIONS; i++) {
            questions[i] = new Question("Q" + i, "A" + i);
        }
        QuestionSource source = new StaticQuestionSource(questions);
        ChatSession session = new ChatSession(source, evaluator, TOTAL_QUESTIONS);

        for (int i = 1; i < TOTAL_QUESTIONS; i++) {
            session.nextQuestion(); // Задаем вопросы с 1-го по 9-й
            assertFalse(session.isFinished(), "Тест не должен завершиться после " + i + " заданных вопросов");
        }

        session.nextQuestion();

        assertTrue(session.isFinished(), "Тест ДОЛЖЕН быть закончен после того, как задано 10 вопросов");
    }

    @Test
    @DisplayName("[TIMED_TEST] Корректно отображает прогресс по ЗАДАННЫМ вопросам")
    void timedTest_showsProgressCorrectly() {
        QuestionSource source = new StaticQuestionSource(Q1, Q2);
        ChatSession session = new ChatSession(source, evaluator, 2);

        assertEquals("Вопрос 1 из 2", session.getTestProgress());

        session.nextQuestion();

        assertEquals("Вопрос 2 из 2", session.getTestProgress());

        session.nextQuestion();

        assertEquals("Вопрос 2 из 2", session.getTestProgress());
    }

    @Test
    @DisplayName("[SPRINT] При неправильном ответе НЕ показывает правильный ответ")
    void sprintMode_handlesIncorrectAnswer() {
        QuestionSource source = new StaticQuestionSource(Q1);
        ChatSession session = new ChatSession(source, evaluator, ChatSession.GameMode.SPRINT);
        session.nextQuestion();

        EvaluationResult result = session.evaluate("неверный ответ");

        assertFalse(result.isCorrect());
        assertEquals("Неправильно.", result.getMessage());
    }
}