package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурационный класс для бота.
 *
 * @param telegramToken Токен Telegram для доступа к API бота.
 * @param scrapperUrl URL сервиса-скраппера.
 */
@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken, @NotEmpty String scrapperUrl) {}
