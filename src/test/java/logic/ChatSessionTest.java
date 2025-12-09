package logic;

import questions.Question;
import questions.StaticQuestionSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ChatSessionTest {
    @Test
    public void flowHelpAndAnswers() {
        ChatSession s = new ChatSession(new StaticQuestionSource(Arrays.asList(
                new Question("Q1", "A1"),
                new Question("Q2", "A2")
        )), new SimpleEvaluator(), false);

        assertTrue(s.intro().contains("Привет!"));
        assertEquals("Q1", s.nextQuestion());

        assertTrue(s.help().contains("Помощь"));

        assertEquals("Correct!", s.evaluate("A1").getMessage());
        assertEquals(1, s.correctCount());
        assertEquals(1, s.askedCount());

        assertEquals("Q2", s.nextQuestion());
        assertTrue(s.evaluate("wrong").getMessage().contains("Incorrect"));
        assertEquals(1, s.correctCount());
    }
}
