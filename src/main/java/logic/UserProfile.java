package logic;

public class UserProfile {

    private final String username;

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

    @Override
    public String toString() {
        return String.format(
                """
                
                --- Личный кабинет пользователя [%s] ---
                > Правильных ответов: %d / %d
                > Процент верных ответов: %.1f%%
                > Лучшая серия: %d
                ------------------------------------
                """,
                username,
                totalCorrectAnswers,
                totalQuestionsAnswered,
                getWinRate(),
                bestStreak
        );
    }
}
