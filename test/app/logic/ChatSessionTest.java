package app.logic;

import app.questions.Question;
import app.questions.StaticQuestionSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ChatSessionTest {
    @Test
    public void flowHelpAndAnswers() {
        ChatSession s = new ChatSession(new StaticQuestionSource(Arrays.asList(
                new Question("Q1", "A1"),
                new Question("Q2", "A2")
        )), new SimpleEvaluator());

        assertTrue(s.intro().contains("Привет"));
        assertEquals("Q1", s.nextQuestion());

        String help = s.evaluate("\\help");
        assertTrue(help.contains("\\help"));

        assertEquals("Верно!", s.evaluate("A1"));
        assertEquals(1, s.correctCount());
        assertEquals(1, s.askedCount());

        assertEquals("Q2", s.nextQuestion());
        assertTrue(s.evaluate("wrong").startsWith("Неверно"));
        assertEquals(1, s.correctCount());
    }
}