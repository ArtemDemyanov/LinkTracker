package backend.academy.bot;

import backend.academy.bot.config.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** Основной класс приложения бота. Этот класс запускает Spring Boot приложение и настраивает Telegram бота. */
@SpringBootApplication
@EnableConfigurationProperties({BotConfig.class})
public class BotApplication {

    private static final Logger logger = LoggerFactory.getLogger(BotApplication.class);

    /**
     * Точка входа в приложение.
     *
     * @param args Аргументы командной строки.
     */
    public static void main(String[] args) {
        var context = SpringApplication.run(BotApplication.class, args);

        // Получаем конфигурацию бота
        BotConfig botConfig = context.getBean(BotConfig.class);
        TelegramBot bot = new TelegramBot(botConfig.telegramToken());

        logger.atInfo()
                .setMessage("Bot application started")
                .addKeyValue("telegramToken", botConfig.telegramToken())
                .log();

        // Регистрируем команды бота
        bot.execute(new SetMyCommands(
                new BotCommand("/start", "Регистрация пользователя"),
                new BotCommand("/help", "Список доступных команд"),
                new BotCommand("/track", "Начать отслеживание ссылки"),
                new BotCommand("/untrack", "Прекратить отслеживание ссылки"),
                new BotCommand("/list", "Показать список отслеживаемых ссылок")));

        logger.atInfo().setMessage("Bot commands registered").log();
    }
}
