package logic;

import questions.Question;
import questions.QuestionSource;

public final class ChatSession {

	public enum GameMode {
		REGULAR,    // Тренировка: показываем ответ, ждем правильного ввода
		TIMED_TEST, // Тест: не показываем ответ, ждем правильного ввода
		SPRINT      // Спринт: не показываем ответ, даем следующий вопрос
	}

	private final QuestionSource source;
	private final Evaluator evaluator;

	private Question current;
	private int correct = 0;
	private int asked = 0;

	private final GameMode gameMode;
	private final int totalTestQuestions;
	private long startTime;

	public ChatSession(QuestionSource source, Evaluator evaluator, GameMode mode) {
		if (mode == GameMode.TIMED_TEST) {
			throw new IllegalArgumentException("Для режима TIMED_TEST используйте другой конструктор.");
		}
		this.gameMode = mode;
		this.totalTestQuestions = 0;
		this.source = source;
		this.evaluator = evaluator;
	}

	public ChatSession(QuestionSource source, Evaluator evaluator, int questionCount) {
		this.gameMode = GameMode.TIMED_TEST;
		this.totalTestQuestions = questionCount;
		this.evaluator = evaluator;
		this.source = source;
	}

	public EvaluationResult evaluate(String userInput) {
		if (current == null) {
			return new EvaluationResult(false, "Ошибка: сначала нужно получить вопрос.");
		}
		boolean isCorrect = evaluator.isCorrect(current, userInput);
		if (isCorrect) {
			correct++;
		}

		String message;
		switch (gameMode) {
			case REGULAR:
				message = isCorrect ? "Правильно!" : "Неправильно. Верный ответ: " + current.answer();
				break;
			case TIMED_TEST:
			case SPRINT:
			default:
				message = isCorrect ? "Правильно!" : "Неправильно.";
				break;
		}
		return new EvaluationResult(isCorrect, message);
	}

	public String nextQuestion() {
		asked++;
		try {
			current = source.next();
			return current.text();
		} catch (Exception e) {
			current = null;
			return null;
		}
	}

	public String currentQuestion() {
		return current == null ? "Нет активного вопроса." : current.text();
	}

	public GameMode getGameMode() { return gameMode; }
	public int getCorrectCount() { return correct; }
	public void startTimer() { this.startTime = System.currentTimeMillis(); }

	public boolean isFinished() {
		if (gameMode == GameMode.TIMED_TEST) {
			return asked >= totalTestQuestions;
		}
		return false;
	}

	public long getElapsedTime() {
		if (gameMode == GameMode.TIMED_TEST || gameMode == GameMode.SPRINT) {
			return System.currentTimeMillis() - startTime;
		}
		return 0;
	}

	public String getTestProgress() {
		if (gameMode == GameMode.TIMED_TEST) {
			return String.format("Вопрос %d из %d", Math.min(asked + 1, totalTestQuestions), totalTestQuestions);
		}
		return "";
	}

	public String intro() {
		return """
        Привет! Я буду задавать задачи по математике.
        Чтобы посмотреть правила — введите /help.
        Чтобы посмотреть свой профиль — введите /profile.
        Чтобы выйти — введите /exit.
        """;
	}
	public String help() {
		return """
        --- Помощь ---
        Что делать:
        — Я показываю вопрос. Ваша задача — ввести ответ и нажать Enter.

        Команды:
        — /help    — показать это сообщение.
        — /profile — показать вашу статистику.
        — /exit    — выйти из программы и сохранить прогресс.
        --------------
        """;
	}
}