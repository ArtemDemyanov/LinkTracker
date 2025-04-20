package backend.academy.bot;

import backend.academy.bot.config.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableConfigurationProperties(BotConfig.class)
@EnableCaching
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}

@Component
class BotInitializer {
    private static final Logger logger = LoggerFactory.getLogger(BotInitializer.class);
    private final TelegramBot bot;

    public BotInitializer(TelegramBot bot) {
        this.bot = bot;
    }

    @PostConstruct
    public void init() {
        initializeBot();
    }

    private void initializeBot() {
        logger.atInfo()
                .setMessage("Bot application started")
                .addKeyValue("telegramToken", bot.getToken())
                .log();

        bot.execute(new SetMyCommands(
                new BotCommand("/start", "Регистрация пользователя"),
                new BotCommand("/help", "Список доступных команд"),
                new BotCommand("/track", "Начать отслеживание ссылки"),
                new BotCommand("/untrack", "Прекратить отслеживание ссылки"),
                new BotCommand("/list", "Показать список отслеживаемых ссылок")));

        logger.atInfo().setMessage("Bot commands registered").log();
    }
}
