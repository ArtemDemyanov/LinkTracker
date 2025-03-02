package backend.academy.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/** Конфигурация WebClient. Этот класс создает и настраивает экземпляр WebClient для взаимодействия с внешними API. */
@Configuration
public class WebClientConfig {

    /**
     * Создает и возвращает экземпляр WebClient.Builder.
     *
     * @return Экземпляр WebClient.Builder.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
