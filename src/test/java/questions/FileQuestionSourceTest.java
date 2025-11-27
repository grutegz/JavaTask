package questions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class FileQuestionSourceTest {

    @TempDir
    Path tempDir;

    @Test
    public void readsAndCyclesQuestionsFromFileCorrectly() throws IOException {

        Path questionsFile = tempDir.resolve("questions.txt");
        List<String> lines = List.of(
                "2+2|4",
                "Столица России?|Москва"
        );
        Files.write(questionsFile, lines);

        FileQuestionSource source = new FileQuestionSource(questionsFile, "\\|");

        Question q1 = source.next();
        assertEquals("2+2", q1.text());
        assertEquals("4", q1.answer());

        Question q2 = source.next();
        assertEquals("Столица России?", q2.text());
        assertEquals("Москва", q2.answer());

        Question q3 = source.next();
        assertEquals("2+2", q3.text(), "После последнего вопроса должен снова идти первый.");
    }

    @Test
    public void constructorThrowsExceptionIfFileNotFound() {

        Path nonExistentFile = tempDir.resolve("non_existent.txt");

        assertThrows(RuntimeException.class, () -> {
            new FileQuestionSource(nonExistentFile, "\\|");
        }, "Должно быть брошено исключение, если файл не найден.");
    }

    @Test
    public void constructorThrowsExceptionIfFileIsEmpty() throws IOException {

        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);

        assertThrows(IllegalArgumentException.class, () -> {
            new FileQuestionSource(emptyFile, "\\|");
        }, "Должно быть брошено исключение, если файл пуст.");
    }

    @Test
    public void constructorThrowsExceptionIfFileContainsOnlyInvalidLines() throws IOException {

        Path invalidFile = tempDir.resolve("invalid.txt");
        List<String> lines = List.of(
                "просто текст без разделителя",
                "и еще один"
        );
        Files.write(invalidFile, lines);

        assertThrows(IllegalArgumentException.class, () -> {
            new FileQuestionSource(invalidFile, "\\|");
        }, "Должно быть брошено исключение, если в файле нет валидных строк.");
    }
}