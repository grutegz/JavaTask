package logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserProfileTest {

    private UserProfile profile;

    @BeforeEach
    public void setUp() {
        profile = new UserProfile("tester");
    }

    @Test
    public void newProfileHasZeroStats() {

        assertEquals(0, profile.getTotalQuestionsAnswered(), "У нового профиля общее число ответов должно быть 0");
        assertEquals(0, profile.getTotalCorrectAnswers(), "У нового профиля число правильных ответов должно быть 0");
        assertEquals(0, profile.getBestStreak(), "У нового профиля лучшая серия должна быть 0");
        assertEquals(0.0, profile.getWinRate(), "У нового профиля процент побед должен быть 0.0");
    }

    @Test
    public void oneCorrectAnswerUpdatesStatsCorrectly() {

        profile.processAnswer(true);

        assertEquals(1, profile.getTotalQuestionsAnswered());
        assertEquals(1, profile.getTotalCorrectAnswers());
        assertEquals(1, profile.getBestStreak(), "После первого правильного ответа лучшая серия должна стать 1");
    }

    @Test
    public void oneIncorrectAnswerUpdatesStatsCorrectly() {

        profile.processAnswer(false);

        assertEquals(1, profile.getTotalQuestionsAnswered());
        assertEquals(0, profile.getTotalCorrectAnswers(), "Число правильных ответов не должно меняться после неправильного");
        assertEquals(0, profile.getBestStreak());
    }

    @Test
    public void streakIsUpdatedAndResetCorrectly() {

        profile.processAnswer(true);
        profile.processAnswer(true);

        assertEquals(2, profile.getBestStreak(), "Лучшая серия должна быть 2 после двух верных ответов");

        profile.processAnswer(false);

        assertEquals(2, profile.getBestStreak(), "Лучшая серия не должна сбрасываться после неверного ответа");

        profile.processAnswer(true);

        assertEquals(2, profile.getBestStreak());
    }

    @Test
    public void bestStreakIsOverwrittenByLongerStreak() {

        profile.processAnswer(true);
        profile.processAnswer(false);
        profile.processAnswer(true);
        profile.processAnswer(true);
        profile.processAnswer(true);

        assertEquals(3, profile.getBestStreak());
    }

    @Test
    public void winRateIsCalculatedCorrectly() {

        assertEquals(0.0, profile.getWinRate());

        profile.processAnswer(true);
        profile.processAnswer(false);
        assertEquals(50.0, profile.getWinRate());

        profile.processAnswer(true);
        profile.processAnswer(false);
        assertEquals(50.0, profile.getWinRate());

        profile.processAnswer(true);
        assertEquals(60.0, profile.getWinRate());
    }
}