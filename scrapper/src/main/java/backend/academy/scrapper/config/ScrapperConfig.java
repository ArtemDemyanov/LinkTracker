package backend.academy.scrapper.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        AppProperties app,
        GitHubProperties github,
        StackOverflowProperties stackoverflow,
        SchedulingProperties scheduling) {
    public record AppProperties(
            @NotEmpty String accessType, @NotEmpty String botUrl, @NotEmpty String messageTransport) {}

    public record GitHubProperties(
            @NotEmpty String token,
            @NotEmpty String baseUrl,
            @NotNull Duration connectionTimeout,
            @NotNull Duration readTimeout,
            @Min(0) int maxRetries,
            @NotNull Duration retryDelay) {}

    public record StackOverflowProperties(
            @NotEmpty String baseUrl,
            @NotNull Duration connectionTimeout,
            @NotNull Duration readTimeout,
            @Min(0) int maxRetries,
            @NotNull Duration retryDelay,
            ApiCredentials api) {
        public record ApiCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}
    }

    public record SchedulingProperties(@Positive int batchSize) {}
}
