package backend.academy.scrapper.client;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.StackOverflowProperties;
import backend.academy.scrapper.config.ScrapperConfig.StackOverflowProperties.ApiCredentials;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class StackOverflowClient extends BaseApiClient {
    private final ApiCredentials apiCredentials;
    private final StackOverflowProperties properties;
    private static final Logger logger = LoggerFactory.getLogger(StackOverflowClient.class);

    public StackOverflowClient(WebClient.Builder webClientBuilder, ScrapperConfig config) {
        super(
                webClientBuilder,
                config.stackoverflow().baseUrl(),
                config.stackoverflow().connectionTimeout(),
                config.stackoverflow().readTimeout());
        this.properties = config.stackoverflow();
        this.apiCredentials = properties.api();

        this.webClient
                .mutate()
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + apiCredentials.accessToken())
                .build();
    }

    /**
     * Получает новые ответы на вопрос с указанным ID, созданные после определенного времени.
     *
     * @param questionId ID вопроса на Stack Overflow.
     * @param since Время, после которого должны быть созданы ответы.
     * @return Mono<List<Answer>> Список новых ответов.
     */
    public Mono<List<Answer>> getNewAnswers(String questionId, Instant since) {
        return webClient
                .get()
                .uri(uri -> uri.path("/questions/{id}/answers")
                        .queryParam("site", "stackoverflow")
                        .queryParam("order", "desc")
                        .queryParam("sort", "creation")
                        .queryParam("fromdate", since.getEpochSecond())
                        .queryParam("filter", "withbody")
                        .queryParam("access_token", apiCredentials.accessToken())
                        .queryParam("key", apiCredentials.key())
                        .build(questionId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(AnswerResponse.class)
                .map(response -> response.items.stream()
                        .filter(a -> Instant.ofEpochSecond(a.creationDate).isAfter(since))
                        .collect(Collectors.toList()))
                .retryWhen(createRetryPolicy());
    }

    /**
     * Получает новые комментарии к вопросу с указанным ID, созданные после определенного времени.
     *
     * @param questionId ID вопроса на Stack Overflow.
     * @param since Время, после которого должны быть созданы комментарии.
     * @return Mono<List<Comment>> Список новых комментариев.
     */
    public Mono<List<Comment>> getNewComments(String questionId, Instant since) {
        return webClient
                .get()
                .uri(uri -> uri.path("/questions/{id}/comments")
                        .queryParam("site", "stackoverflow")
                        .queryParam("order", "desc")
                        .queryParam("sort", "creation")
                        .queryParam("fromdate", since.getEpochSecond())
                        .queryParam("filter", "withbody")
                        .queryParam("access_token", apiCredentials.accessToken())
                        .queryParam("key", apiCredentials.key())
                        .build(questionId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .bodyToMono(CommentResponse.class)
                .map(response -> response.items.stream()
                        .filter(c -> Instant.ofEpochSecond(c.creationDate).isAfter(since))
                        .collect(Collectors.toList()))
                .retryWhen(createRetryPolicy());
    }

    /**
     * Создает политику повторных попыток для запросов к Stack Overflow API.
     *
     * @return Retry Политика повторных попыток с экспоненциальным откатом.
     */
    private Retry createRetryPolicy() {
        return Retry.backoff(properties.maxRetries(), properties.retryDelay())
                .filter(this::isRetryableError)
                .doBeforeRetry(
                        retry -> logger.warn("Retrying StackOverflow API call, attempt {}", retry.totalRetries() + 1));
    }

    /**
     * Проверяет, является ли ошибка допустимой для повторной попытки.
     *
     * @param throwable Исключение, возникшее при выполнении запроса.
     * @return true, если ошибка допустима для повторной попытки; иначе false.
     */
    private boolean isRetryableError(Throwable throwable) {
        return throwable instanceof WebClientResponseException response
                && (response.getStatusCode().is5xxServerError()
                        || response.getStatusCode() == HttpStatusCode.valueOf(429));
    }

    /**
     * Извлекает ID вопроса из URL Stack Overflow.
     *
     * @param url URL вопроса на Stack Overflow.
     * @return ID вопроса.
     * @throws StackOverflowClientException Если URL имеет неверный формат.
     */
    public String extractQuestionId(URI url) {
        String[] parts = url.getPath().split("/", -1);
        if (parts.length < 3 || !"questions".equals(parts[1])) {
            throw new StackOverflowClientException("Invalid Stack Overflow URL: " + url, HttpStatusCode.valueOf(400));
        }
        return parts[2];
    }

    /**
     * Обрабатывает ошибки, возникающие при выполнении запроса к Stack Overflow API.
     *
     * @param response Ответ от сервера, содержащий информацию об ошибке.
     * @return Mono<Throwable> Исключение, содержащее детали ошибки.
     */
    private Mono<Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new StackOverflowClientException(
                        "StackOverflow API error: " + response.statusCode() + " - " + body, response.statusCode())));
    }

    @Getter
    @Setter
    public static class AnswerResponse {
        @JsonProperty("items")
        private List<Answer> items = Collections.emptyList();
    }

    @Getter
    @Setter
    public static class CommentResponse {
        @JsonProperty("items")
        private List<Comment> items = Collections.emptyList();
    }

    public static class Answer {
        @JsonProperty("creation_date")
        public long creationDate;

        public String body = "";

        public Owner owner = new Owner();

        @JsonProperty("answer_id")
        public long answerId;
    }

    public static class Comment {
        @JsonProperty("creation_date")
        public long creationDate;

        public String body = "";

        public Owner owner = new Owner();

        @JsonProperty("comment_id")
        public long commentId;
    }

    public static class Owner {
        @JsonProperty("display_name")
        public String displayName;

        @JsonProperty("user_id")
        public long userId;
    }

    @Getter
    public static class StackOverflowClientException extends RuntimeException {
        private final HttpStatusCode statusCode;

        /**
         * Конструктор исключения.
         *
         * @param message Сообщение об ошибке.
         * @param statusCode HTTP-статус код ошибки.
         */
        public StackOverflowClientException(String message, HttpStatusCode statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
    }
}
