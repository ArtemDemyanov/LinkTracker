package backend.academy.bot.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {

    private final BotConfig botConfig;

    /**
     * Конфигурация для создания экземпляра Telegram бота.
     *
     * @param botConfig Конфигурация бота, содержащая токен Telegram.
     */
    public TelegramBotConfig(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    /**
     * Создает и возвращает экземпляр Telegram бота.
     *
     * @return Экземпляр Telegram бота.
     */
    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(botConfig.telegramToken());
    }
}
