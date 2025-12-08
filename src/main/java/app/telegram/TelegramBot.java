package app.telegram;

import app.ConfigLoader;
import logic.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import questions.*;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramBot extends TelegramLongPollingBot {

    private final UserProfileService profileService;
    private final Map<Long, ChatSession> chatSessions = new ConcurrentHashMap<>();
    private final ConfigLoader config;
    private final QuestionSource questionSource = new RandomQuestionSource(
            new FileQuestionSource(Path.of("questions.txt"), "\\|"),
            new GeometricSequenceQuestionSource(),
            new ArithmeticSumQuestionSource(),
            new GeometricSumQuestionSource()
    );

    public TelegramBot(UserProfileService profileService, ConfigLoader config) {
        this.profileService = profileService;
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String textFromUser = update.getMessage().getText();

            String profileKey = String.valueOf(chatId);
            UserProfile profile = profileService.getProfile(profileKey);

            ChatSession session = chatSessions.computeIfAbsent(chatId, id ->
                    new ChatSession(questionSource, new SimpleEvaluator(), true));

            if (textFromUser.startsWith("/")) {
                handleCommand(chatId, textFromUser, profile, session);
            } else {
                handleAnswer(chatId, textFromUser, profile, session);
            }
        }
    }

    private void handleCommand(long chatId, String command, UserProfile profile, ChatSession session) {
        String responseText = switch (command) {
            case "/start" ->
                    session.intro() + "\n\nЧтобы начать, введите /quiz.\nЧтобы посмотреть статистику, введите /profile.";
            case "/help" -> session.help();
            case "/quiz" -> "Новый вопрос: " + session.nextQuestion();
            case "/profile" -> profile.toString();
            default -> "Неизвестная команда. Используйте /help для списка команд.";
        };
        sendMessage(chatId, responseText);
    }

    private void handleAnswer(long chatId, String answer, UserProfile profile, ChatSession session) {
        EvaluationResult result = session.evaluate(answer);
        profile.processAnswer(result.isCorrect());

        String feedbackMessage = result.getMessage();
        String nextQuestionMessage = "Следующий вопрос: " + session.nextQuestion();
        sendMessage(chatId, feedbackMessage + "\n\n" + nextQuestionMessage);
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}