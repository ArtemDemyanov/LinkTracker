package backend.academy.bot.config;

import backend.academy.bot.config.RetryProperties;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Configuration
public class BotRetryConfig {

    @Bean
    public RetryRegistry retryRegistry(RetryProperties retryProperties) {
        RetryRegistry registry = RetryRegistry.ofDefaults();

        for (Map.Entry<String, RetryProperties.RetryInstanceProperties> entry : retryProperties.instances().entrySet()) {
            String name = entry.getKey();
            RetryProperties.RetryInstanceProperties props = entry.getValue();

            Set<Integer> retryableStatuses = props.retryableStatuses();

            RetryConfig config = RetryConfig.custom()
                .maxAttempts(props.maxAttempts())
                .waitDuration(Duration.parse(props.waitDuration()))
                .retryOnException(throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        int status = ex.getStatusCode().value();
                        if (status == 429) {
                            return ex.getHeaders().containsKey("Retry-After");
                        }
                        return retryableStatuses.contains(status);
                    }
                    return false;
                })
                .build();

            registry.retry(name, config);
        }

        return registry;
    }
}
