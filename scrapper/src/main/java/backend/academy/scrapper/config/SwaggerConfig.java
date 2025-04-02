package backend.academy.scrapper.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Конфигурация Swagger для документирования API.
     *
     * @return Конфигурация для публичного API.
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("backend.academy.scrapper.controller")
                .build();
    }
}
