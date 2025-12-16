package logic;

import java.time.Instant;

public class UserProfile {

    private final String username;

    private long bestTestTimeMillis = -1;
    private Instant lastTestAttempt;
    private int sprintBestScore = 0;

    private int totalQuestionsAnswered = 0;
    private int totalCorrectAnswers = 0;
    private int currentStreak = 0;
    private int bestStreak = 0;

    public UserProfile(String username) {
        this.username = username;
    }

    public void processAnswer(boolean isCorrect) {
        totalQuestionsAnswered++;
        if (isCorrect) {
            totalCorrectAnswers++;
            currentStreak++;
            bestStreak = Math.max(bestStreak, currentStreak);
        } else {
            currentStreak = 0;
        }
    }

    public String getUsername() { return username; }
    public int getTotalQuestionsAnswered() { return totalQuestionsAnswered; }
    public int getTotalCorrectAnswers() { return totalCorrectAnswers; }
    public int getBestStreak() { return bestStreak; }
    public double getWinRate() {
        if (totalQuestionsAnswered == 0) return 0.0;
        return (double) totalCorrectAnswers / totalQuestionsAnswered * 100.0;
    }
    public long getBestTestTimeMillis() {
        return bestTestTimeMillis;
    }

    public void setBestTestTimeMillis(long time) {
        if (this.bestTestTimeMillis == -1 || time < this.bestTestTimeMillis) {
            this.bestTestTimeMillis = time;
        }
    }

    public int getSprintBestScore() {
        return sprintBestScore;
    }

    public void setSprintBestScore(int score) {
        if (score > this.sprintBestScore) {
            this.sprintBestScore = score;
        }
    }

    public Instant getLastTestAttempt() {
        return lastTestAttempt;
    }

    public void setLastTestAttempt(Instant time) {
        this.lastTestAttempt = time;
    }

    public boolean isTestAvailable() {
        return lastTestAttempt == null;
    }

    @Override
    public String toString() {
        String testInfo = bestTestTimeMillis == -1
                ? "Тест на время еще не пройден."
                : String.format("Лучшее время в тесте: %.2f сек.", bestTestTimeMillis / 1000.0);

        String sprintInfo = "Лучший результат в спринте: " + sprintBestScore + " очков.";
        return String.format(
                """
                --- Личный кабинет пользователя [%s] ---
                Правильных ответов: %d / %d
                Процент верных ответов: %.1f%%
                Лучшая серия: %d
                ------------------------------------
                Рекорды:
                %s
                %s
                """,
                username, totalCorrectAnswers, totalQuestionsAnswered, getWinRate(), bestStreak,
                testInfo,
                sprintInfo
        );
    }
}
