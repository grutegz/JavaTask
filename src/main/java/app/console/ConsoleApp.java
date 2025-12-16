package app.console;

import logic.*;
import questions.*;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Scanner;

public class ConsoleApp {

    public static void main(String[] args) {
        // Инициализация сервисов
        UserProfileService profileService = new UserProfileService();
        Runtime.getRuntime().addShutdownHook(new Thread(profileService::saveProfilesToFile));
        LeaderboardService leaderboardService = new LeaderboardService(profileService);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в консольный математический квиз!");
        System.out.print("Введите ваше имя пользователя: ");
        String username = scanner.nextLine().trim();
        UserProfile profile = profileService.getProfile(username);
        System.out.printf("Привет, %s! Ваш прошлый прогресс загружен.%n", username);

        // Главное меню
        while (true) {
            System.out.println("\n--- Главное меню ---");
            System.out.println("1. Тренировочный квиз (бесконечный)");
            System.out.println("2. Тест на время (10 вопросов)");
            System.out.println("3. Спринт (60 секунд)");
            System.out.println("4. Показать таблицу рекордов");
            System.out.println("5. Показать мой профиль");
            System.out.println("6. Выход");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    startRegularQuiz(scanner, profile);
                    break;
                case "2":
                    startTestQuiz(scanner, profile);
                    break;
                case "3":
                    startSprintQuiz(scanner, profile);
                    break;
                case "4":
                    showLeaderboardMenu(scanner, profile, leaderboardService);
                    break;
                case "5":
                    System.out.println(profile.toString());
                    break;
                case "6":
                    System.out.println("Спасибо за игру!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Неверный ввод. Пожалуйста, выберите пункт от 1 до 6.");
                    break;
            }
        }
    }

    private static QuestionSource createQuestionSource() {
        return new RandomQuestionSource(
                new FileQuestionSource(Path.of("questions.txt"), "\\|"),
                new GeometricSequenceQuestionSource(),
                new ArithmeticSumQuestionSource(),
                new GeometricSumQuestionSource()
        );
    }

    private static void startRegularQuiz(Scanner scanner, UserProfile profile) {
        System.out.println("\n--- Тренировочный квиз ---");
        ChatSession session = new ChatSession(createQuestionSource(), new SimpleEvaluator(), ChatSession.GameMode.REGULAR);
        System.out.println("Чтобы остановить квиз, введите \\stop");
        System.out.println("Вопрос: " + session.nextQuestion());

        while (true) {
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("\\stop")) {
                System.out.println("Тренировка завершена.");
                return;
            }

            EvaluationResult result = session.evaluate(userInput);
            System.out.println(result.getMessage());

            profile.processAnswer(result.isCorrect());

            System.out.println("Следующий вопрос: " + session.nextQuestion());

        }
    }

    private static void startTestQuiz(Scanner scanner, UserProfile profile) {
        if (!profile.isTestAvailable()) {
            System.out.println("Вы уже проходили этот тест.");
            return;
        }

        System.out.println("\n--- Тест на время (10 вопросов) ---");
        ChatSession session = new ChatSession(createQuestionSource(), new SimpleEvaluator(), 10);
        session.startTimer();
        profile.setLastTestAttempt(Instant.now());

        System.out.println("Чтобы прервать тест, введите \\stop");
        System.out.println(session.getTestProgress() + "\nВопрос: " + session.nextQuestion());

        while (!session.isFinished()) {
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("\\stop")) { /* ... */ return; }

            EvaluationResult result = session.evaluate(userInput);
            System.out.println(result.getMessage());

            if (result.isCorrect()) {
                if (!session.isFinished()) {
                    System.out.println(session.getTestProgress() + "\nВопрос: " + session.nextQuestion());
                }
            }
        }

        long elapsedTime = session.getElapsedTime();
        profile.setBestTestTimeMillis(elapsedTime);
        System.out.printf("Тест завершен! Ваш результат: %d из 10. Время: %.2f сек.\n", session.getCorrectCount(), elapsedTime / 1000.0);
    }

    private static void startSprintQuiz(Scanner scanner, UserProfile profile) {
        System.out.println("\n--- Спринт (60 секунд) ---");
        System.out.println("Дайте как можно больше правильных ответов. Поехали!");

        ChatSession session = new ChatSession(createQuestionSource(), new SimpleEvaluator(), ChatSession.GameMode.SPRINT);
        session.startTimer();

        System.out.println("Чтобы остановить спринт, введите \\stop");
        System.out.println("Вопрос: " + session.nextQuestion());

        long sprintDurationMillis = 60 * 1000;

        while (session.getElapsedTime() < sprintDurationMillis) {
            String userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("\\stop")) { /* ... */ return; }

            EvaluationResult result = session.evaluate(userInput);
            System.out.println(result.getMessage());

            if (session.getElapsedTime() < sprintDurationMillis) {
                System.out.println("Следующий вопрос: " + session.nextQuestion());
            } else {
                break;
            }
        }

        int score = session.getCorrectCount();
        profile.setSprintBestScore(score);
        System.out.println("\nВремя вышло! Ваш результат: " + score + " правильных ответов.");
    }

    private static void showLeaderboardMenu(Scanner scanner, UserProfile profile, LeaderboardService leaderboardService) {
        System.out.println("\n--- Таблица рекордов ---");
        System.out.println("1. По кол-ву правильных ответов");
        System.out.println("2. По лучшей серии");
        System.out.println("3. По времени в тесте");
        System.out.println("4. По очкам в спринте");
        System.out.println("5. Назад в главное меню");
        System.out.print("Выберите таблицу: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                System.out.println(leaderboardService.getTotalCorrectAnswersLeaderboard(profile));
                break;
            case "2":
                System.out.println(leaderboardService.getBestStreakLeaderboard(profile));
                break;
            case "3":
                System.out.println(leaderboardService.getTestTimeLeaderboard(profile));
                break;
            case "4":
                System.out.println(leaderboardService.getSprintLeaderboard(profile));
                break;
            case "5":
                return;
            default:
                System.out.println("Неверный ввод.");
                break;
        }
    }
}