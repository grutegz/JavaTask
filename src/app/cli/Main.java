package app.console;

import app.logic.ChatSession;
import app.logic.SimpleEvaluator;
import app.questions.Question;
import app.questions.StaticQuestionSource;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public final class Main {
	public static void main(String[] args) {
		List<Question> qs = Arrays.asList(
		new Question("x^2 - 5x + 6 = 0",  "3"),//2, 3
		new Question("x^2 - 9x + 14 = 0", "7"),//2, 7
		new Question("x^2 - x - 6 = 0",   "3"),//-2, 3
		new Question("x^2 - 8x + 15 = 0", "5"),//3,5
		new Question("x^2 + x - 12 = 0",  "3"),//-4, 3
		new Question("x^2 - 13x + 36 = 0","9"),//4, 9
		new Question("x^2 - 10x + 24 = 0","6"),//4, 6
		new Question("x^2 - 3x - 28 = 0", "7"),//-4, 7
		new Question("x^2 - 15x + 54 = 0","9"),//6, 9
		new Question("x^2 - 2x - 63 = 0","9")//-7, 9
		);
		ChatSession session = new ChatSession(new StaticQuestionSource(qs), new SimpleEvaluator());
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
