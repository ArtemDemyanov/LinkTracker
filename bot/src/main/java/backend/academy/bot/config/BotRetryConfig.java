package backend.academy.bot.config;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Configuration
public class BotRetryConfig {

    private static final Set<Integer> RETRYABLE_STATUSES = Set.of(500, 502, 503, 504, 429);

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryOnException(throwable -> throwable instanceof WebClientResponseException ex
                        && RETRYABLE_STATUSES.contains(ex.getStatusCode().value()))
                .build();

        return RetryRegistry.of(config);
    }
}
