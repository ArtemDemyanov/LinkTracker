package backend.academy.scrapper;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация приложения Scrapper. Этот класс содержит настройки для взаимодействия с GitHub и Stack Overflow.
 *
 * @param githubToken Токен для доступа к API GitHub.
 * @param stackOverflow Настройки для доступа к API Stack Overflow.
 * @param githubBaseUrl Базовый URL для API GitHub.
 */
@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        @NotEmpty String githubToken,
        StackOverflowCredentials stackOverflow,
        String githubBaseUrl,
        String stackOverflowBaseUrl // Новое поле для базового URL GitHub
        ) {
    /**
     * Настройки для доступа к API Stack Overflow.
     *
     * @param key Ключ для доступа к API Stack Overflow.
     * @param accessToken Токен для доступа к API Stack Overflow.
     */
    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}
}
