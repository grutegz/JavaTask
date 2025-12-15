package logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Финальные тесты для LeaderboardService.
 * Использует "поддельную" реализацию UserProfileService для полной изоляции.
 */
public class LeaderboardServiceTest {

    // Вложенный класс-заглушка для изоляции от файловой системы
    private static class FakeUserProfileService extends UserProfileService {
        private List<UserProfile> profilesToReturn = Collections.emptyList();
        // Пустой конструктор, чтобы не читать файлы
        public FakeUserProfileService() { super(); }
        // Метод для "программирования" заглушки
        public void setProfilesToReturn(List<UserProfile> profiles) { this.profilesToReturn = profiles; }
        // Переопределяем метод, который будет вызываться
        @Override
        public Collection<UserProfile> getAllProfiles() { return profilesToReturn; }
    }

    private FakeUserProfileService fakeProfileService;
    private LeaderboardService leaderboardService;

    // Тестовые пользователи
    private UserProfile alice, bob, charlie, david;

    @BeforeEach
    void setUp() {
        fakeProfileService = new FakeUserProfileService();
        leaderboardService = new LeaderboardService(fakeProfileService);

        alice = new UserProfile("Alice");
        alice.setBestTestTimeMillis(15000); // 15 сек
        alice.setSprintBestScore(10);
        alice.processAnswer(true); alice.processAnswer(true); // 2 прав. ответа

        bob = new UserProfile("Bob");
        bob.setBestTestTimeMillis(12000); // 12 сек
        bob.setSprintBestScore(12);
        bob.processAnswer(true); bob.processAnswer(true); bob.processAnswer(true); // 3 прав. ответа

        charlie = new UserProfile("Charlie");
        charlie.setSprintBestScore(5);
        charlie.processAnswer(true); // 1 прав. ответ

        david = new UserProfile("David");
        david.setBestTestTimeMillis(20000);
    }

    @Test
    @DisplayName("[Тест на время] Таблица рекордов правильно отсортирована по возрастанию")
    void getTestTimeLeaderboard_isSortedCorrectly() {
        fakeProfileService.setProfilesToReturn(List.of(alice, bob, david));
        String leaderboard = leaderboardService.getTestTimeLeaderboard(bob);
        String[] lines = leaderboard.split("\n");

        assertTrue(lines[2].startsWith("1. Bob"), "Bob (12s) должен быть на 1-м месте.");
        assertTrue(lines[2].contains("(Это вы)"), "У Боба должна быть пометка.");
        assertTrue(lines[3].startsWith("2. Alice"), "Alice (15s) должна быть на 2-м месте.");
        assertTrue(lines[4].startsWith("3. David"), "David (20s) должен быть на 3-м месте.");
    }

    @Test
    @DisplayName("[Спринт] Таблица рекордов правильно отсортирована по убыванию")
    void getSprintLeaderboard_isSortedCorrectly() {
        fakeProfileService.setProfilesToReturn(List.of(alice, bob, charlie));
        String leaderboard = leaderboardService.getSprintLeaderboard(alice);
        String[] lines = leaderboard.split("\n");

        assertTrue(lines[2].startsWith("1. Bob"), "Bob (12 очков) должен быть на 1-м месте.");
        assertTrue(lines[3].startsWith("2. Alice"), "Alice (10 очков) должна быть на 2-м месте.");
        assertTrue(lines[3].contains("(Это вы)"), "У Alice должна быть пометка.");
        assertTrue(lines[4].startsWith("3. Charlie"), "Charlie (5 очков) должен быть на 3-м месте.");
    }

    @Test
    @DisplayName("Возвращает сообщение 'нет рекордов', если список пуст")
    void getLeaderboard_returnsEmptyMessage_whenNoRecords() {
        fakeProfileService.setProfilesToReturn(Collections.emptyList());
        String leaderboard = leaderboardService.getTestTimeLeaderboard(alice);
        assertTrue(leaderboard.contains("В этой категории рекордов пока нет"));
    }

    @Test
    @DisplayName("Корректно показывает позицию пользователя вне топ-5")
    void getLeaderboard_showsUserPosition_outsideTop5() {
        // Arrange: Создаем 6 пользователей
        UserProfile user1 = new UserProfile("User1"); user1.setSprintBestScore(20);
        UserProfile user2 = new UserProfile("User2"); user2.setSprintBestScore(18);
        UserProfile user3 = new UserProfile("User3"); user3.setSprintBestScore(16);
        UserProfile user4 = new UserProfile("User4"); user4.setSprintBestScore(14);
        UserProfile user5 = new UserProfile("User5"); user5.setSprintBestScore(12);
        UserProfile user6_you = new UserProfile("You"); user6_you.setSprintBestScore(10);

        fakeProfileService.setProfilesToReturn(List.of(user1, user2, user3, user4, user5, user6_you));

        // Act: Запрашиваем лидерборд для 6-го пользователя
        String leaderboard = leaderboardService.getSprintLeaderboard(user6_you);

        // Assert
        assertTrue(leaderboard.contains("5. User5 - 12 очков\n"), "Пятый игрок должен быть в топе");
        assertTrue(leaderboard.contains("...\n6. You - 10 очков (Это вы)"), "Ваша 6-я позиция должна быть показана в конце");
    }

    @Test
    @DisplayName("Показывает сообщение, если у пользователя нет рекорда в данной категории")
    void getLeaderboard_showsInfoMessage_ifUserHasNoRecord() {
        // Arrange: David не играл в спринт
        fakeProfileService.setProfilesToReturn(List.of(alice, bob));

        // Act: Запрашиваем для него таблицу спринта
        String leaderboard = leaderboardService.getSprintLeaderboard(david);

        // Assert
        assertTrue(leaderboard.contains("Вашего рекорда еще нет в этой таблице"));
    }
}