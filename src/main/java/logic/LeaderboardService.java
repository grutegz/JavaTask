package logic;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LeaderboardService {

    private final UserProfileService userProfileService;

    public LeaderboardService(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    public String getTotalCorrectAnswersLeaderboard(UserProfile currentUser) {
        return generateLeaderboardText(
                "Топ-5 по правильным ответам:",
                profile -> profile.getTotalCorrectAnswers() > 0,
                profile -> (long) profile.getTotalCorrectAnswers(),
                score -> String.format("%d", score),
                currentUser,
                false
        );
    }

    public String getBestStreakLeaderboard(UserProfile currentUser) {
        return generateLeaderboardText(
                "Топ-5 по лучшей серии ответов:",
                profile -> profile.getBestStreak() > 0,
                profile -> (long) profile.getBestStreak(),
                score -> String.format("%d", score),
                currentUser,
                false
        );
    }

    public String getTestTimeLeaderboard(UserProfile currentUser) {
        return generateLeaderboardText(
                "Топ-5 по времени в тесте:",
                profile -> profile.getBestTestTimeMillis() != -1,
                UserProfile::getBestTestTimeMillis,
                score -> String.format("%.2f сек.", score / 1000.0),
                currentUser,
                true
        );
    }

    public String getSprintLeaderboard(UserProfile currentUser) {
        return generateLeaderboardText(
                "Топ-5 в режиме 'Спринт':",
                profile -> profile.getSprintBestScore() > 0,
                profile -> (long) profile.getSprintBestScore(),
                score -> String.format("%d очков", score),
                currentUser,
                false
        );
    }

    private String generateLeaderboardText(String title,
                                           Predicate<UserProfile> filter,
                                           Function<UserProfile, Long> scoreExtractor,
                                           Function<Long, String> scoreFormatter,
                                           UserProfile currentUser,
                                           boolean ascending) {

        List<LeaderboardEntry> entries = userProfileService.getAllProfiles().stream()
                .filter(filter)
                .map(p -> new LeaderboardEntry(p.getUsername(), scoreExtractor.apply(p)))
                .collect(Collectors.toList());

        Comparator<LeaderboardEntry> comparator = Comparator.comparingLong(LeaderboardEntry::getScore);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        entries.sort(comparator);

        if (entries.isEmpty()) {
            return "В этой категории рекордов пока нет.";
        }

        StringBuilder sb = new StringBuilder("🏆 " + title + " 🏆\n\n");

        // --- ЛОГИКА ОТОБРАЖЕНИЯ (ПОЛНОСТЬЮ ПЕРЕПИСАНА) ---

        int userPosition = -1;
        // Находим позицию текущего пользователя
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getUsername().equals(currentUser.getUsername())) {
                userPosition = i;
                break;
            }
        }

        // Показываем до 5 лучших игроков
        int topSize = Math.min(5, entries.size());
        for (int i = 0; i < topSize; i++) {
            LeaderboardEntry entry = entries.get(i);
            String suffix = (i == userPosition) ? " (Это вы)" : "";
            sb.append(String.format("%d. %s - %s%s\n",
                    i + 1, entry.getUsername(), scoreFormatter.apply(entry.getScore()), suffix));
        }

        // Если пользователь существует в рейтинге, но не попал в отображаемый топ,
        // добавляем его позицию в конце.
        if (userPosition != -1 && userPosition >= topSize) {
            sb.append("...\n");
            sb.append(String.format("%d. %s - %s (Это вы)\n",
                    userPosition + 1,
                    currentUser.getUsername(),
                    scoreFormatter.apply(scoreExtractor.apply(currentUser))));
        } else if (userPosition == -1 && filter.test(currentUser)) {
            // Этот случай почти невозможен, но для полноты картины
        } else if (!filter.test(currentUser)) {
            sb.append("\nВашего рекорда еще нет в этой таблице.");
        }

        return sb.toString();
    }
}