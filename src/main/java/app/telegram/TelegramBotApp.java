package app.telegram;

import app.ConfigLoader;
import logic.UserProfileService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TelegramBotApp {
    public static void main(String[] args) {
        try {
            ConfigLoader config = new ConfigLoader("config.properties");

            UserProfileService profileService = new UserProfileService();
            Runtime.getRuntime().addShutdownHook(new Thread(profileService::saveProfilesToFile));

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            botsApi.registerBot(new TelegramBot(profileService, config));

            System.out.println("Telegram-бот успешно запущен!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}