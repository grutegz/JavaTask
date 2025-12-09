package app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    public void loadsPropertiesFromFileSuccessfully() throws IOException {
        Path configFile = tempDir.resolve("test_config.properties");
        String content = """
                         bot.name = TestBot
                         bot.token = 12345:test_token
                         """;
        Files.writeString(configFile, content);


        ConfigLoader configLoader = new ConfigLoader(configFile);

        assertEquals("TestBot", configLoader.getBotName());
        assertEquals("12345:test_token", configLoader.getBotToken());
    }

    @Test
    public void constructorThrowsExceptionIfFileNotFound() {
        Path nonExistentFile = tempDir.resolve("non_existent.properties");
        assertThrows(FileNotFoundException.class, () -> {
            new ConfigLoader(nonExistentFile);
        }, "Должно быть брошено FileNotFoundException, если файл не найден.");
    }
}