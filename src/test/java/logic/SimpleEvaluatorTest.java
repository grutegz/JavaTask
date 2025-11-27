package logic;

import questions.Question;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleEvaluatorTest {
    @Test
    public void caseAndTrimIgnored() {
        SimpleEvaluator e = new SimpleEvaluator();
        Question q = new Question("Q", "Paris");
        assertTrue(e.isCorrect(q, "  paris "));
        assertFalse(e.isCorrect(q, "london"));
    }
}