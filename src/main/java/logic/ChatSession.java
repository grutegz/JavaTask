package logic;

import questions.Question;
import questions.QuestionSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class ChatSession {
	private static final int BUFFER_SIZE = 20;

	private final QuestionSource source;
	private final Evaluator evaluator;
    private final boolean shuffleQs;

	private final Random rng = new Random();
	private final List<Question> questionList = new ArrayList<>();

	private Question current;
	private int asked = 0;
	private int correct = 0;

	public ChatSession(QuestionSource source, Evaluator evaluator, boolean shuffleQs) {
		if (source == null || evaluator == null) throw new IllegalArgumentException();
		this.source = source;
		this.evaluator = evaluator;
        this.shuffleQs = shuffleQs;
	}

    public String intro() {
	return "Hi! I will give you quadratic equations (ax^2 + bx + c = 0).\n"
		 + "Your task: type ONLY the larger root. Type \\help for details.";
    }

    public String help() {
	return "What to do:\n"
		 + "- I show a quadratic equation like  x^2 - 5x + 6 = 0\n"
		 + "- Find both roots and send the larger one\n\n"
		 + "Input format:\n"
		 + "- Just the number (f.e. \"4.5\",\"-9\")\n\n"
		 + "Commands:\n"
		 + "- \\help - show this message\n";
    }

	public String nextQuestion() {
		ensureList();
		current = questionList.remove(0);
		asked++;
		return current.text();
	}

	public String currentQuestion() {
		return current == null ? "" : current.text();
	}

	public String evaluate(String userInput) {
		if (userInput != null && userInput.trim().equals("\\help")) return help();
		if (current == null) return "Wait for question.";
		boolean ok = evaluator.isCorrect(current, userInput);
		if (ok) {
			correct++;
			return "Correct!";
		}
		return "Incorrect. Correct answer: " + current.answer();
	}

	public int askedCount() {
		return asked;
	}

	public int correctCount() {
		return correct;
	}

    private void ensureList() {
        if (questionList.isEmpty()) {
            for (int i = 0; i < BUFFER_SIZE; i++) {
                questionList.add(source.next());
            }
            if (shuffleQs) {
                Collections.shuffle(questionList, rng);
            }
        }
    }
}
