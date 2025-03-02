package backend.academy.scrapper.client;

import backend.academy.scrapper.ScrapperConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Клиент для взаимодействия с API Stack Overflow. Этот класс предоставляет методы для получения информации о последней
 * активности вопроса.
 */
@Component
public class StackOverflowClient {

    private static final Logger logger = LoggerFactory.getLogger(StackOverflowClient.class);
    private final WebClient webClient;
    private final String soAccessToken;
    private final String stackOverflowKey;

    /**
     * Конструктор класса StackOverflowClient.
     *
     * @param webClientBuilder Builder для создания экземпляра WebClient.
     * @param config Конфигурация Scrapper, содержащая токен и ключ Stack Overflow.
     */
    public StackOverflowClient(WebClient.Builder webClientBuilder, ScrapperConfig config) {
        this.soAccessToken = config.stackOverflow().accessToken();
        this.stackOverflowKey = config.stackOverflow().key();
        this.webClient = webClientBuilder
                .baseUrl(config.stackOverflowBaseUrl())
                .filter(logRequest())
                .build();
    }

    /**
     * Получает время последней активности вопроса по его URL.
     *
     * @param url URL вопроса Stack Overflow.
     * @return Mono<Long>, содержащий время последней активности.
     */
    public Mono<Long> getLastActivityDate(URI url) {
        String[] pathParts = url.getPath().split("/");
        if (pathParts.length < 3 || !pathParts[1].equals("questions")) {
            logger.atError()
                    .setMessage("Invalid Stack Overflow question URL")
                    .addKeyValue("url", url)
                    .log();
            return Mono.error(new IllegalArgumentException("Некорректный URL вопроса Stack Overflow"));
        }
        String questionId = pathParts[2];

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/2.3/questions/{questionId}")
                        .queryParam("site", "stackoverflow")
                        .queryParam("access_token", soAccessToken)
                        .queryParam("key", stackOverflowKey)
                        .build(questionId))
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + soAccessToken)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(responseBody -> logger.atDebug()
                        .setMessage("Stack Overflow API response")
                        .addKeyValue("questionId", questionId)
                        .addKeyValue("responseBody", responseBody)
                        .log())
                .map(responseBody -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(responseBody);
                    } catch (JsonProcessingException e) {
                        logger.atError()
                                .setMessage("Failed to parse Stack Overflow API response")
                                .addKeyValue("questionId", questionId)
                                .addKeyValue("responseBody", responseBody)
                                .log();
                        throw new RuntimeException(e);
                    }
                    JsonNode itemsNode = jsonNode.get("items");
                    if (itemsNode == null || itemsNode.isEmpty()) {
                        logger.atError()
                                .setMessage("Missing or empty items field in Stack Overflow API response")
                                .addKeyValue("questionId", questionId)
                                .log();
                        throw new RuntimeException("Поле items отсутствует или пустое в ответе Stack Overflow API");
                    }
                    JsonNode lastActivityDateNode = itemsNode.get(0).get("last_activity_date");
                    if (lastActivityDateNode == null || lastActivityDateNode.isNull()) {
                        logger.atError()
                                .setMessage("Missing last_activity_date field in Stack Overflow API response")
                                .addKeyValue("questionId", questionId)
                                .log();
                        throw new RuntimeException("Поле last_activity_date отсутствует в ответе Stack Overflow API");
                    }
                    return lastActivityDateNode.asLong();
                })
                .doOnError(error -> {
                    logger.atError()
                            .setMessage("Error fetching data from Stack Overflow")
                            .addKeyValue("questionId", questionId)
                            .addKeyValue("error", error.getMessage())
                            .log();
                    if (error instanceof WebClientResponseException webClientError) {
                        logger.atError()
                                .setMessage("Stack Overflow API error response")
                                .addKeyValue("statusCode", webClientError.getStatusCode())
                                .addKeyValue("responseBody", webClientError.getResponseBodyAsString())
                                .log();
                    }
                });
    }

    /**
     * Логирует запросы к API Stack Overflow.
     *
     * @return ExchangeFilterFunction для логирования запросов.
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.atDebug()
                    .setMessage("Sending request to Stack Overflow API")
                    .addKeyValue("url", clientRequest.url())
                    .log();
            return Mono.just(clientRequest);
        });
    }
}
