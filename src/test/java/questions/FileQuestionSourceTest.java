package questions;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileQuestionSourceTest {

    @TempDir
    Path tempDir;

    @Test
    void readsQuestionsFromFile() throws IOException {
        // временный файл с вопросами
        Path file = tempDir.resolve("test.txt");
        List<String> lines = List.of("Q1|A1","Q2|A2");
        Files.write(file, lines);
        FileQuestionSource src = new FileQuestionSource(file, "\\|");

        Question q1 = src.next();
        assertEquals("Q1", q1.text());
        assertEquals("A1", q1.answer());

        Question q2 = src.next();
        assertEquals("Q2", q2.text());
        assertEquals("A2", q2.answer());
    }
    @Test
    void throwsIfFileEmpty() throws IOException {
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> new FileQuestionSource(emptyFile, "\\|"));
        assertTrue(ex.getMessage().contains("No valid questions"));
    }

    @Test
    void throwsIfFileMissing() {
        Path noneFile = tempDir.resolve("nope.txt");
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> new FileQuestionSource(noneFile, "\\|"));
        assertTrue(ex.getMessage().contains("Failed to read"));
    }
}