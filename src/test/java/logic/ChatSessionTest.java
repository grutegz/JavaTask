package logic;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;

import questions.FileQuestionSource;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatSessionTest {
    @TempDir
    Path tempDir;

    @Test
    public void flowHelpAndAnswers() throws IOException {
        Path file = tempDir.resolve("test.txt");
        List<String> lines = List.of("Q1|A1","Q2|A2");
        Files.write(file, lines);

        ChatSession s = new ChatSession(new FileQuestionSource(file, "\\|"),
                new SimpleEvaluator(), false);

        assertTrue(s.intro().contains("Hi"));
        assertEquals("Q1", s.nextQuestion());

        String help = s.evaluate("\\help");
        assertTrue(help.contains("\\help"));

        assertEquals("Correct!", s.evaluate("A1"));
        assertEquals(1, s.correctCount());
        assertEquals(1, s.askedCount());

        assertEquals("Q2", s.nextQuestion());
        assertTrue(s.evaluate("wrong").startsWith("Incorrect."));
        assertEquals(1, s.correctCount());
    }
}
