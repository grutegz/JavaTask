package logic;

import questions.Question;
import questions.QuestionSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class ChatSession {
	private static final int BUFFER_SIZE = 20; // Количество вопросов, которые предзагружаются в буфер

	private final QuestionSource source;
	private final Evaluator evaluator;
	private final boolean shuffleQs;

	private final Random rng = new Random();
	private final List<Question> questionList = new ArrayList<>(); // Буфер для предзагруженных вопросов

	private Question current;
	private int asked = 0;
	private int correct = 0;

	public ChatSession(QuestionSource source, Evaluator evaluator, boolean shuffleQs) {
		if (source == null || evaluator == null) throw new IllegalArgumentException("Source and evaluator must not be null");
		this.source = source;
		this.evaluator = evaluator;
		this.shuffleQs = shuffleQs;
	}

	public String intro() {
		return """
    Привет! Я буду задавать задачи по математике.
    Чтобы посмотреть правила — введите \\help.
    Чтобы посмотреть свой профиль — введите \\profile.
    Чтобы выйти — введите \\exit.
    """;
	}

	public String help() {
		return """
    --- Помощь ---
    Что делать:
    — Я показываю вопрос. Ваша задача — ввести ответ и нажать Enter.

    Типы задач:
    1) Квадратное уравнение: ax^2 + bx + c = 0
       Что вводить: больший корень уравнения.
       Пример: «x^2 - 5x + 6 = 0» → корни 2 и 3 → ответ: 3

    2) Геометрическая прогрессия (a_n):
       Дано: a1, r, n. Найдите a_n.

    3) Сумма арифметической прогрессии (S_n):
       Дано: a1, d, n. Найдите S_n.

    4) Сумма геометрической прогрессии (S_n):
       Дано: a1, r, n. Найдите S_n.

    Команды:
    — \\help    — показать это сообщение.
    — \\profile — показать вашу статистику.
    — \\exit    — выйти из программы и сохранить прогресс.
    --------------
    """;
	}

	public EvaluationResult evaluate(String userInput) {
		if (current == null) {
			return new EvaluationResult(false, "Сначала получите вопрос.");
		}
		boolean ok = evaluator.isCorrect(current, userInput);
		if (ok) {
			correct++;
			return new EvaluationResult(true, "Correct!");
		}
		return new EvaluationResult(false, "Incorrect. Correct answer: " + current.answer());
	}

	public String nextQuestion() {
		ensureList();
		current = questionList.removeFirst();
		asked++;
		return current.text();
	}

	public String currentQuestion() {
		return current == null ? "Нет активного вопроса." : current.text();
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

	public int askedCount() {
		return asked;
	}

	public int correctCount() {
		return correct;
	}
}