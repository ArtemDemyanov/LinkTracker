package backend.academy.scrapper.config;

import io.github.resilience4j.retry.Retry;
import java.time.Duration;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Configuration
public class RetryConfig {

    private static final Set<Integer> RETRYABLE_STATUSES = Set.of(500, 502, 503, 504, 429);

    @Bean
    public Retry retry() {
        io.github.resilience4j.retry.RetryConfig config = io.github.resilience4j.retry.RetryConfig.<Object>custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryOnException(throwable -> {
                    if (throwable instanceof WebClientResponseException response) {
                        return RETRYABLE_STATUSES.contains(
                                response.getStatusCode().value());
                    }
                    return false;
                })
                .build();

        return Retry.of("githubRetry", config);
    }
}
