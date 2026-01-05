package logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserProfileService {

    private static final Path STORAGE_PATH = Path.of("user_profiles.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, UserProfile> userProfiles;

    public UserProfileService() {
        this.userProfiles = loadProfilesFromFile();
    }

    public UserProfile getProfile(String username) {
        return userProfiles.computeIfAbsent(username, UserProfile::new);
    }

    private Map<String, UserProfile> loadProfilesFromFile() {
        try (FileReader reader = new FileReader(STORAGE_PATH.toFile())) {
            Type type = new TypeToken<ConcurrentHashMap<String, UserProfile>>(){}.getType();
            Map<String, UserProfile> profiles = gson.fromJson(reader, type);

            if (profiles == null) return new ConcurrentHashMap<>();
            System.out.println("Успешно загружено " + profiles.size() + " профилей пользователей.");
            return profiles;
        } catch (IOException e) {
            System.out.println("Файл 'user_profiles.json' не найден. Будет создан новый при завершении работы.");
            return new ConcurrentHashMap<>();
        }
    }

    public void saveProfilesToFile() {
        if (userProfiles.isEmpty()) return;
        try (FileWriter writer = new FileWriter(STORAGE_PATH.toFile())) {
            gson.toJson(userProfiles, writer);
            System.out.println("\nДанные " + userProfiles.size() + " пользователей успешно сохранены.");
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении профилей: " + e.getMessage());
        }
    }
}