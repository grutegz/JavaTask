package questions;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class StaticQuestionSourceTest {
    @Test
    public void cyclesSequentially() {
        StaticQuestionSource src = new StaticQuestionSource(Arrays.asList(
                new Question("Q1", "A1"),
                new Question("Q2", "A2")
        ));
        assertEquals("Q1", src.next().text());
        assertEquals("Q2", src.next().text());
        assertEquals("Q1", src.next().text());
    }
}