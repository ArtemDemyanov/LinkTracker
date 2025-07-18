package backend.academy.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "retry")
public record RetryProperties(
    Map<String, RetryInstanceProperties> instances
) {
    public record RetryInstanceProperties(
        int maxAttempts,
        String waitDuration,
        Set<Integer> retryableStatuses
    ) {}
}
