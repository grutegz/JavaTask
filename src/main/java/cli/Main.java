package cli;

import logic.ChatSession;
import logic.SimpleEvaluator;
import questions.FileQuestionSource;
import questions.QuestionSource;

import java.nio.file.Path;
import java.util.Scanner;

public final class Main {
    public static void main(String[] args) {
        QuestionSource source = new FileQuestionSource(Path.of("questions.txt"), "\\|");

        ChatSession session = new ChatSession(source, new SimpleEvaluator(), true);

        Scanner sc = new Scanner(System.in);
        System.out.println(session.intro());
        System.out.println("Question: " + session.nextQuestion());

        while (true) {
            String line = sc.nextLine();
            if (line != null && line.trim().equals("\\help")) {
                System.out.println(session.help());
                System.out.println("Question: " + session.currentQuestion());
            } else {
                System.out.println(session.evaluate(line));
                System.out.println("Question: " + session.nextQuestion());
            }
        }
    }
}