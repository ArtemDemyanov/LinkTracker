package backend.academy.scrapper.client;

import backend.academy.scrapper.client.dto.stackoverflow.Answer;
import backend.academy.scrapper.client.dto.stackoverflow.AnswerResponse;
import backend.academy.scrapper.client.dto.stackoverflow.Comment;
import backend.academy.scrapper.client.dto.stackoverflow.CommentResponse;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.StackOverflowProperties.ApiCredentials;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StackOverflowClient extends BaseApiClient {
    private final ApiCredentials apiCredentials;
    private final Retry stackOverflowRetry;
    private static final Logger logger = LoggerFactory.getLogger(StackOverflowClient.class);

    public StackOverflowClient(WebClient.Builder webClientBuilder, ScrapperConfig config, Retry stackOverflowRetry) {
        super(
                webClientBuilder,
                config.stackoverflow().baseUrl(),
                Map.of(
                        "Accept",
                        MediaType.APPLICATION_JSON_VALUE,
                        "Authorization",
                        "Bearer " + config.stackoverflow().api().accessToken()));
        this.apiCredentials = config.stackoverflow().api();
        this.stackOverflowRetry = stackOverflowRetry;
    }

    @TimeLimiter(name = "stackOverflowClient")
    @CircuitBreaker(name = "stackOverflowClient", fallbackMethod = "fallbackAnswers")
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
                .transformDeferred(RetryOperator.of(stackOverflowRetry));
    }

    @TimeLimiter(name = "stackOverflowClient")
    @CircuitBreaker(name = "stackOverflowClient", fallbackMethod = "fallbackComments")
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
                .transformDeferred(RetryOperator.of(stackOverflowRetry));
    }

    @SuppressWarnings("unused")
    private Mono<List<Answer>> fallbackAnswers(String questionId, Instant since, Throwable t) {
        logger.error("Fallback triggered for getNewAnswers({}, {}): {}", questionId, since, t.toString());
        return Mono.just(List.of());
    }

    @SuppressWarnings("unused")
    private Mono<List<Comment>> fallbackComments(String questionId, Instant since, Throwable t) {
        logger.error("Fallback triggered for getNewComments({}, {}): {}", questionId, since, t.toString());
        return Mono.just(List.of());
    }

    @SuppressWarnings("unused")
    public String extractQuestionId(URI url) {
        String[] parts = url.getPath().split("/", -1);
        if (parts.length < 3 || !"questions".equals(parts[1])) {
            throw new StackOverflowClientException("Invalid Stack Overflow URL: " + url, HttpStatusCode.valueOf(400));
        }
        return parts[2];
    }

    @SuppressWarnings("unused")
    private Mono<Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new StackOverflowClientException(
                        "StackOverflow API error: " + response.statusCode() + " - " + body, response.statusCode())));
    }

    @Getter
    public static class StackOverflowClientException extends RuntimeException {
        private final HttpStatusCode statusCode;

        public StackOverflowClientException(String message, HttpStatusCode statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
    }
}
