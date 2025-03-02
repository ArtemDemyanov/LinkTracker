package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация бота. Этот класс содержит настройки бота, такие как токен Telegram.
 *
 * @param telegramToken Токен Telegram для доступа к API бота.
 */
@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken) {}
