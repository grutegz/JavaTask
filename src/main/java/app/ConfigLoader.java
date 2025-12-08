package app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigLoader {

    private final Properties properties = new Properties();

    public ConfigLoader(String resourceName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new RuntimeException("Файл конфигурации не найден в ресурсах: " + resourceName);
            }
            loadProperties(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла конфигурации из ресурсов.", e);
        }
    }

    ConfigLoader(Path filePath) throws IOException {
        try (InputStream input = new FileInputStream(filePath.toFile())) {
            loadProperties(input);
        }
    }

    private void loadProperties(InputStream input) throws IOException {
        properties.load(input);
    }

    public String getBotName() {
        return properties.getProperty("bot.name");
    }

    public String getBotToken() {
        return properties.getProperty("bot.token");
    }
}