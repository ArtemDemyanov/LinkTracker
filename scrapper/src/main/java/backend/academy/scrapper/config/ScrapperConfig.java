package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
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

    public record GitHubProperties(@NotEmpty String token, @NotEmpty String baseUrl) {}

    public record StackOverflowProperties(@NotEmpty String baseUrl, ApiCredentials api) {
        public record ApiCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}
    }

    public record SchedulingProperties(@Positive int batchSize) {}
}
