package logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class UserProfileTest {

    private UserProfile profile;

    @BeforeEach
    public void setUp() {
        profile = new UserProfile("tester");
    }

    @Test
    @DisplayName("Новый профиль должен иметь нулевую статистику и рекорды")
    public void newProfile_ShouldHaveZeroStatsAndNoRecords() {
        assertEquals(0, profile.getTotalQuestionsAnswered(), "Общее число ответов должно быть 0");
        assertEquals(0, profile.getTotalCorrectAnswers(), "Число правильных ответов должно быть 0");
        assertEquals(0, profile.getBestStreak(), "Лучшая серия должна быть 0");
        assertEquals(0.0, profile.getWinRate(), "Процент побед должен быть 0.0");
        assertEquals(-1, profile.getBestTestTimeMillis(), "Время теста по умолчанию должно быть -1");
        assertEquals(0, profile.getSprintBestScore(), "Счет в спринте по умолчанию должен быть 0");
        assertNull(profile.getLastTestAttempt(), "Время последней попытки должно быть null");
    }

    @Nested
    @DisplayName("Тестирование общей статистики (processAnswer)")
    class GeneralStatsTests {

        @Test
        @DisplayName("Один правильный ответ корректно обновляет все счетчики")
        void onCorrectAnswer_ShouldUpdateAllCounters() {
            profile.processAnswer(true);

            assertEquals(1, profile.getTotalQuestionsAnswered());
            assertEquals(1, profile.getTotalCorrectAnswers());
            assertEquals(1, profile.getBestStreak());
            assertEquals(100.0, profile.getWinRate());
        }

        @Test
        @DisplayName("Один неправильный ответ обновляет только общее число вопросов")
        void onIncorrectAnswer_ShouldUpdateOnlyTotalQuestions() {
            profile.processAnswer(false);

            assertEquals(1, profile.getTotalQuestionsAnswered());
            assertEquals(0, profile.getTotalCorrectAnswers());
            assertEquals(0, profile.getBestStreak());
            assertEquals(0.0, profile.getWinRate());
        }

        @Test
        @DisplayName("Серия ответов корректно обновляет лучшую серию (bestStreak)")
        void onAnswerStreak_ShouldUpdateBestStreak() {
            profile.processAnswer(true); // streak = 1, best = 1
            profile.processAnswer(true); // streak = 2, best = 2
            assertEquals(2, profile.getBestStreak());

            profile.processAnswer(false); // streak = 0, best = 2
            assertEquals(2, profile.getBestStreak(), "Лучшая серия не должна сбрасываться после ошибки");

            profile.processAnswer(true); // streak = 1, best = 2
            assertEquals(2, profile.getBestStreak(), "Лучшая серия не должна меняться, если новая короче");
        }
    }

    @Nested
    @DisplayName("Тестирование рекордов 'Теста на время'")
    class TimedTestRecords {

        @Test
        @DisplayName("Устанавливает время, если рекорда не было")
        void setBestTestTime_ShouldSetTime_WhenNoPreviousRecord() {
            profile.setBestTestTimeMillis(15000L); // 15 секунд
            assertEquals(15000L, profile.getBestTestTimeMillis());
        }

        @Test
        @DisplayName("Обновляет время, если новое лучше (меньше)")
        void setBestTestTime_ShouldUpdateTime_IfNewTimeIsBetter() {
            profile.setBestTestTimeMillis(20000L);
            profile.setBestTestTimeMillis(15000L);
            assertEquals(15000L, profile.getBestTestTimeMillis());
        }

        @Test
        @DisplayName("НЕ обновляет время, если новое хуже (больше)")
        void setBestTestTime_ShouldNotUpdateTime_IfNewTimeIsWorse() {
            profile.setBestTestTimeMillis(15000L);
            profile.setBestTestTimeMillis(20000L);
            assertEquals(15000L, profile.getBestTestTimeMillis(), "Рекорд не должен был обновиться на худший результат");
        }
    }

    @Nested
    @DisplayName("Тестирование рекордов 'Спринта'")
    class SprintRecords {

        @Test
        @DisplayName("Обновляет счет, если новый лучше (больше)")
        void setSprintBestScore_ShouldUpdateScore_IfNewScoreIsBetter() {
            profile.setSprintBestScore(10);
            profile.setSprintBestScore(12);
            assertEquals(12, profile.getSprintBestScore());
        }

        @Test
        @DisplayName("НЕ обновляет счет, если новый хуже (меньше)")
        void setSprintBestScore_ShouldNotUpdateScore_IfNewScoreIsWorse() {
            profile.setSprintBestScore(10);
            profile.setSprintBestScore(8);
            assertEquals(10, profile.getSprintBestScore(), "Рекорд не должен был обновиться на худший результат");
        }
    }

    @Nested
    @DisplayName("Тестирование доступности теста")
    class TestAvailability {

        @Test
        @DisplayName("Тест доступен для нового пользователя")
        void isTestAvailable_ShouldBeTrue_ForNewUser() {
            assertTrue(profile.isTestAvailable(), "Тест должен быть доступен для нового пользователя");
        }

        @Test
        @DisplayName("Тест становится недоступен после попытки")
        void isTestAvailable_ShouldBeFalse_AfterAttempt() {
            profile.setLastTestAttempt(Instant.now());
            assertFalse(profile.isTestAvailable(), "Тест не должен быть доступен после попытки");
        }
    }
}