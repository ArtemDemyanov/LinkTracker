package backend.academy.scrapper.client;

import backend.academy.scrapper.client.dto.GitHubItem;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.GitHubProperties;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class GitHubClient extends BaseApiClient {
    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);
    private final GitHubProperties properties;

    /**
     * @param webClientBuilder Строитель для создания экземпляра WebClient.
     * @param config Конфигурация приложения, содержащая настройки для GitHub API.
     */
    public GitHubClient(WebClient.Builder webClientBuilder, ScrapperConfig config) {
        super(
                webClientBuilder,
                config.github().baseUrl(),
                config.github().connectionTimeout(),
                config.github().readTimeout());
        this.properties = config.github();

        this.webClient
                .mutate()
                .defaultHeader("Authorization", "Bearer " + properties.token())
                .build();
    }

    /**
     * Выполняет запрос к GitHub API для получения элементов репозитория.
     *
     * @param endpoint Конечная точка API (например, issues, pull requests).
     * @param owner Владелец репозитория.
     * @param repo Название репозитория.
     * @param afterTime Время, после которого должны быть созданы элементы.
     * @return Mono<List<GitHubItem>> Список элементов, соответствующих условиям фильтрации.
     */
    public Mono<List<GitHubItem>> fetchGitHubItems(String endpoint, String owner, String repo, Instant afterTime) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/" + endpoint)
                        .queryParam("state", "open")
                        .queryParam("sort", "created")
                        .queryParam("direction", "desc")
                        .build(owner, repo))
                .retrieve()
                .onStatus(this::isError, this::handleError)
                .bodyToFlux(GitHubItem.class)
                .filter(item -> item.createdAt().isAfter(afterTime))
                .collectList()
                .retryWhen(createRetryPolicy());
    }

    /**
     * Создает политику повторных попыток для запросов к GitHub API.
     *
     * @return Retry Политика повторных попыток с экспоненциальным откатом.
     */
    private Retry createRetryPolicy() {
        return Retry.backoff(properties.maxRetries(), properties.retryDelay())
                .doBeforeRetry(retry -> logger.warn("Retrying GitHub API call, attempt {}", retry.totalRetries() + 1));
    }

    /**
     * Проверяет, является ли HTTP-статус ошибкой.
     *
     * @param status HTTP-статус ответа.
     * @return true, если статус указывает на ошибку клиента или сервера; иначе false.
     */
    private boolean isError(HttpStatusCode status) {
        return status.is4xxClientError() || status.is5xxServerError();
    }

    /**
     * Обрабатывает ошибки, возникающие при выполнении запроса к GitHub API.
     *
     * @param response Ответ от сервера, содержащий информацию об ошибке.
     * @return Mono<Throwable> Исключение, содержащее детали ошибки.
     */
    private Mono<Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new GitHubApiException(
                        "GitHub API error: " + response.statusCode() + " - " + body, response.statusCode())));
    }

    /** Исключение, представляющее ошибку при взаимодействии с GitHub API. */
    @Getter
    public static class GitHubApiException extends RuntimeException {
        private final HttpStatusCode statusCode;

        /**
         * Конструктор исключения.
         *
         * @param message Сообщение об ошибке.
         * @param statusCode HTTP-статус код ошибки.
         */
        public GitHubApiException(String message, HttpStatusCode statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
    }
}
