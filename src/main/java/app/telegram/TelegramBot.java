package app.telegram;

import app.ConfigLoader;
import logic.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import questions.*;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TelegramBot extends TelegramLongPollingBot {

    private final ConfigLoader config;
    private final UserProfileService profileService;
    private final LeaderboardService leaderboardService;

    private enum UserState { IDLE, IN_QUIZ }
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, ChatSession> quizSessions = new ConcurrentHashMap<>();

    private final QuestionSource questionSource = new RandomQuestionSource(
            new FileQuestionSource(Path.of("questions.txt"), "\\|"),
            new GeometricSequenceQuestionSource(),
            new ArithmeticSumQuestionSource(),
            new GeometricSumQuestionSource()
    );

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TelegramBot(UserProfileService profileService, ConfigLoader config) {
        this.profileService = profileService;
        this.config = config;
        this.leaderboardService = new LeaderboardService(profileService);
    }

    @Override
    public String getBotUsername() { return config.getBotName(); }

    @Override
    public String getBotToken() { return config.getBotToken(); }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String textFromUser = update.getMessage().getText();
            UserProfile profile = profileService.getProfile(String.valueOf(chatId));
            UserState currentState = userStates.getOrDefault(chatId, UserState.IDLE);

            if (currentState == UserState.IN_QUIZ) {
                handleQuizState(chatId, textFromUser, profile);
            } else { // IDLE
                handleIdleState(chatId, textFromUser, profile);
            }
        }
    }

    private void handleIdleState(long chatId, String command, UserProfile profile) {
        if (!command.startsWith("/")) {
            sendMessage(chatId, "Чтобы начать, используйте одну из команд. /help для списка.");
            return;
        }

        switch (command) {
            case "/start", "/help":
                sendMessage(chatId, """
                        Привет! Я бот для математических квизов.
                        Доступные команды:
                        /quiz - тренировочный режим (бесконечный).
                        /test_quiz - тест на время (10 вопросов).
                        /sprint - спринт на 60 секунд (максимум ответов).
                        /leaderboard - посмотреть таблицу рекордов.
                        /profile - посмотреть вашу статистику.
                        /stop - (в игре) остановить текущий квиз.
                        """);
                break;
            case "/profile":
                sendMessage(chatId, profile.toString());
                break;
            case "/leaderboard":
                sendMessage(chatId, """
                    Какую таблицу рекордов вы хотите посмотреть?
                    /lb_answers - По кол-ву правильных ответов
                    /lb_streak - По лучшей серии
                    /lb_time - По времени в тесте
                    /lb_sprint - По очкам в спринте
                    """);
                break;
            case "/lb_answers":
                sendMessage(chatId, leaderboardService.getTotalCorrectAnswersLeaderboard(profile));
                break;
            case "/lb_streak":
                sendMessage(chatId, leaderboardService.getBestStreakLeaderboard(profile));
                break;
            case "/lb_time":
                sendMessage(chatId, leaderboardService.getTestTimeLeaderboard(profile));
                break;
            case "/lb_sprint":
                sendMessage(chatId, leaderboardService.getSprintLeaderboard(profile));
                break;
            case "/quiz":
                userStates.put(chatId, UserState.IN_QUIZ);
                ChatSession regularSession = new ChatSession(questionSource, new SimpleEvaluator(), ChatSession.GameMode.REGULAR);
                quizSessions.put(chatId, regularSession);
                sendMessage(chatId, "Начинаем тренировочный квиз! Чтобы закончить, введите /stop.");
                sendMessage(chatId, "Вопрос: " + regularSession.nextQuestion());
                break;
            case "/test_quiz":
                if (profile.isTestAvailable()) {
                    userStates.put(chatId, UserState.IN_QUIZ);
                    ChatSession testSession = new ChatSession(questionSource, new SimpleEvaluator(), 10);
                    testSession.startTimer();
                    quizSessions.put(chatId, testSession);
                    profile.setLastTestAttempt(Instant.now());
                    sendMessage(chatId, "Начинаем тест на время из 10 вопросов! Удачи!");
                    sendMessage(chatId, testSession.getTestProgress() + "\nВопрос: " + testSession.nextQuestion());
                } else {
                    sendMessage(chatId, "Вы уже проходили этот тест. Новая попытка будет доступна позже.");
                }
                break;
            case "/sprint":
                startSprintQuiz(chatId);
                break;
            default:
                sendMessage(chatId, "Неизвестная команда.");
                break;
        }
    }

    private void startSprintQuiz(long chatId) {
        userStates.put(chatId, UserState.IN_QUIZ);

        ChatSession sprintSession = new ChatSession(questionSource, new SimpleEvaluator(), ChatSession.GameMode.SPRINT);
        sprintSession.startTimer();
        quizSessions.put(chatId, sprintSession);

        sendMessage(chatId, "Начинаем спринт! У вас 60 секунд. Поехали!");
        sendMessage(chatId, "Вопрос: " + sprintSession.nextQuestion());

        Runnable stopTask = () -> {
            ChatSession currentSession = quizSessions.get(chatId);
            if (currentSession != null && currentSession.getGameMode() == ChatSession.GameMode.SPRINT) {
                UserProfile profile = profileService.getProfile(String.valueOf(chatId));
                int score = currentSession.getCorrectCount();
                profile.setSprintBestScore(score);

                sendMessage(chatId, "Время вышло! Ваш результат в спринте: " + score + " правильных ответов.");

                userStates.put(chatId, UserState.IDLE);
                quizSessions.remove(chatId);
            }
        };

        scheduler.schedule(stopTask, 60, TimeUnit.SECONDS);
    }

    private void handleQuizState(long chatId, String text, UserProfile profile) {
        ChatSession session = quizSessions.get(chatId);
        if (session == null) {
            userStates.put(chatId, UserState.IDLE);
            sendMessage(chatId, "Произошла ошибка сессии. Квиз остановлен.");
            return;
        }

        if (text.equals("/stop")) {
            userStates.put(chatId, UserState.IDLE);
            quizSessions.remove(chatId);
            sendMessage(chatId, "Квиз остановлен.");
            return;
        }

        EvaluationResult result = session.evaluate(text);
        sendMessage(chatId, result.getMessage());

        switch (session.getGameMode()) {
            case TIMED_TEST:
                if (result.isCorrect()) {
                    if (session.isFinished()) {
                        long elapsedTime = session.getElapsedTime();
                        profile.setBestTestTimeMillis(elapsedTime);
                        sendMessage(chatId, String.format("Тест завершен! Ваш результат: %d из 10. Время: %.2f сек.", session.getCorrectCount(), elapsedTime / 1000.0));

                        userStates.put(chatId, UserState.IDLE);
                        quizSessions.remove(chatId);
                    } else {
                        sendMessage(chatId, session.getTestProgress() + "\nВопрос: " + session.nextQuestion());
                    }
                }
                break;

            case SPRINT:
                String nextSprintQuestion = session.nextQuestion();
                if (nextSprintQuestion != null) {
                    sendMessage(chatId, "Следующий вопрос: " + nextSprintQuestion);
                }
                break;

            case REGULAR:
                profile.processAnswer(result.isCorrect());
                sendMessage(chatId, "Следующий вопрос: " + session.nextQuestion());
                break;
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}