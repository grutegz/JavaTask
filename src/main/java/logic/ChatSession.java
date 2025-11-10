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
		return """
    Привет! Я буду задавать задачи по математике: 
    — квадратные уравнения, 
    — прогрессии (геометрическая), 
    — суммы первых n членов (AP/GP).
    Чтобы посмотреть правила и примеры — введите \\help.
    """;
	}

	public String help() {
		return """
    Что делать:
    — Я показываю вопрос. Твоя задача — ввести ответ (обычно целое число) и нажать Enter.

    Типы задач:
    1) Квадратное уравнение: ax^2 + bx + c = 0
       Что вводить: больший корень уравнения.
       Пример: «x^2 - 5x + 6 = 0» → корни 2 и 3 → ответ: 3

    2) Геометрическая прогрессия (a_n):
       Дано: a1, r, n. Найдите a_n = a1 · r^(n−1).

    3) Сумма арифметической прогрессии (S_n):
       Дано: a1, d, n. Найдите S_n = n/2 · (2a1 + (n−1)d).

    4) Сумма геометрической прогрессии (S_n):
       Дано: a1, r, n. Найдите S_n = a1 + a1·r + ... + a1·r^(n−1).

    Формат ответа:
    — Вводите целое число, без лишних пробелов (например: 7, -12, 0).
    — На текущем этапе все ответы целые.

    Команда:
    — \\help — показать это сообщение.
    """;
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
