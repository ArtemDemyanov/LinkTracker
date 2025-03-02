package backend.academy.scrapper;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Конфигурация Swagger для документации API. Этот класс настраивает Swagger для отображения документации API. */
@Configuration
public class SwaggerConfig {

    /**
     * Создает и возвращает конфигурацию для публичного API.
     *
     * @return Конфигурация для публичного API.
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("backend.academy.scrapper.controller") // Укажите пакет с контроллерами
                .build();
    }
}
