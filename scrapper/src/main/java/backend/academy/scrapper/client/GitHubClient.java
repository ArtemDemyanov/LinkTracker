package backend.academy.scrapper.client;

import backend.academy.scrapper.ScrapperConfig;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Клиент для взаимодействия с API GitHub. Этот класс предоставляет методы для получения информации о последнем
 * обновлении репозитория.
 */
@Component
public class GitHubClient {

    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);
    private final WebClient webClient;

    /**
     * Конструктор класса GitHubClient.
     *
     * @param webClientBuilder Builder для создания экземпляра WebClient.
     * @param config Конфигурация Scrapper, содержащая токен GitHub и базовый URL.
     */
    public GitHubClient(WebClient.Builder webClientBuilder, ScrapperConfig config) {
        String githubToken = config.githubToken();
        this.webClient = webClientBuilder
                .baseUrl(config.githubBaseUrl())
                .defaultHeader("Authorization", "Bearer " + githubToken)
                .build();
    }

    /**
     * Получает время последнего обновления репозитория по его URL.
     *
     * @param url URL репозитория GitHub.
     * @return Mono<String>, содержащий время последнего обновления.
     */
    public Mono<String> getLastUpdated(URI url) {
        String[] pathParts = url.getPath().split("/");
        if (pathParts.length < 3) {
            logger.atError()
                    .setMessage("Invalid repository URL")
                    .addKeyValue("url", url)
                    .log();
            return Mono.error(new IllegalArgumentException("Некорректный URL репозитория"));
        }
        String owner = pathParts[1];
        String repo = pathParts[2];

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/repos/{owner}/{repo}").build(owner, repo))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    return response.bodyToMono(String.class).flatMap(errorBody -> {
                        logger.atError()
                                .setMessage("GitHub API error")
                                .addKeyValue("statusCode", response.statusCode())
                                .addKeyValue("errorBody", errorBody)
                                .log();
                        return Mono.error(new RuntimeException("Ошибка от GitHub API: " + errorBody));
                    });
                })
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> {
                    JsonNode updatedAtNode = jsonNode.get("updated_at");
                    if (updatedAtNode == null || updatedAtNode.isNull()) {
                        logger.atError()
                                .setMessage("Missing updated_at field in GitHub API response")
                                .addKeyValue("url", url)
                                .log();
                        throw new RuntimeException("Поле updated_at отсутствует в ответе GitHub API");
                    }
                    return updatedAtNode.asText();
                });
    }
}
