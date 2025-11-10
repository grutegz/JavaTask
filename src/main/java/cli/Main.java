package cli;

import logic.ChatSession;
import logic.SimpleEvaluator;
import questions.FileQuestionSource;
import questions.QuestionSource;
import questions.Question;
import questions.GeometricSequenceQuestionSource;
import questions.ArithmeticSumQuestionSource;
import questions.GeometricSumQuestionSource;

import java.nio.file.Path;
import java.util.Scanner;

public final class Main {
    public static void main(String[] args) {
        QuestionSource source = new QuestionSource() {
            private final java.util.Random rng = new java.util.Random();
            private final QuestionSource[] sources = new QuestionSource[] {
                    new FileQuestionSource(Path.of("questions.txt"), "\\|"), // твои квадратики из файла
                    new GeometricSequenceQuestionSource(),                   // a_n геометрической прогрессии
                    new ArithmeticSumQuestionSource(),                       // S_n арифметической прогрессии
                    new GeometricSumQuestionSource()                         // S_n геометрической прогрессии
            };
            @Override
            public Question next() {
                return sources[rng.nextInt(sources.length)].next();
            }
        };

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