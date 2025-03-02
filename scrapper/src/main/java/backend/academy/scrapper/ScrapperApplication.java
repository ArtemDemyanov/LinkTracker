package backend.academy.scrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Основной класс приложения Scrapper. Этот класс запускает Spring Boot приложение и включает поддержку планировщика.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ScrapperConfig.class})
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
