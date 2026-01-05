package app.console;

import logic.*;
import questions.*;
import java.nio.file.Path;
import java.util.Scanner;

public class ConsoleApp {
    public static void main(String[] args) {
        // Логика инициализации сервисов и получения профиля
        UserProfileService profileService = new UserProfileService();
        Runtime.getRuntime().addShutdownHook(new Thread(profileService::saveProfilesToFile));
        Scanner scanner = new Scanner(System.in);

        System.out.println("Добро пожаловать в консольный математический квиз!");
        System.out.print("Введите ваше имя пользователя: ");
        String username = scanner.nextLine().trim();
        UserProfile profile = profileService.getProfile(username);
        System.out.printf("Привет, %s! Ваш прошлый прогресс загружен.%n", username);
        System.out.println(profile);

        QuestionSource source = new RandomQuestionSource(
                new FileQuestionSource(Path.of("questions.txt"), "\\|"),
                new GeometricSequenceQuestionSource(),
                new ArithmeticSumQuestionSource(),
                new GeometricSumQuestionSource()
        );

        ChatSession session = new ChatSession(source, new SimpleEvaluator(), true);
        System.out.println(session.intro());
        System.out.println("Question: " + session.nextQuestion());

        while (true) {
            String userInput = scanner.nextLine();
            if (userInput == null) break;

            switch (userInput.trim().toLowerCase()) {
                case "\\exit":
                    System.out.println("Спасибо за игру! Ваш прогресс будет сохранен.");
                    System.exit(0);
                    break;
                case "\\help":
                    System.out.println(session.help());
                    System.out.println("Current question: " + session.currentQuestion());
                    break;
                case "\\profile":
                    System.out.println(profile);
                    System.out.println("Current question: " + session.currentQuestion());
                    break;
                default:
                    EvaluationResult result = session.evaluate(userInput);
                    profile.processAnswer(result.isCorrect());
                    System.out.println(result.getMessage());
                    System.out.println("Question: " + session.nextQuestion());
                    break;
            }
        }
    }
}