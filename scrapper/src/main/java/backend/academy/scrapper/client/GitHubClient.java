package backend.academy.scrapper.client;

import backend.academy.scrapper.client.dto.github.GitHubItem;
import backend.academy.scrapper.config.ScrapperConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GitHubClient extends BaseApiClient {
    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);
    private final Retry githubRetry;

    public GitHubClient(WebClient.Builder webClientBuilder, ScrapperConfig config, Retry githubRetry) {
        super(
                webClientBuilder,
                config.github().baseUrl(),
                Map.of("Authorization", "Bearer " + config.github().token()));
        this.githubRetry = githubRetry;
    }

    @TimeLimiter(name = "githubClient")
    @CircuitBreaker(name = "githubClient", fallbackMethod = "fallbackGitHubItems")
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
                .transformDeferred(RetryOperator.of(githubRetry));
    }

    @SuppressWarnings("unused")
    private Mono<List<GitHubItem>> fallbackGitHubItems(
            String endpoint, String owner, String repo, Instant afterTime, Throwable throwable) {
        logger.error("Fallback triggered for GitHubClient on {}/{} due to: {}", owner, repo, throwable.toString());
        return Mono.just(List.of());
    }

    private boolean isError(HttpStatusCode status) {
        return status.is4xxClientError() || status.is5xxServerError();
    }

    private Mono<Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new GitHubApiException(
                        "GitHub API error: " + response.statusCode() + " - " + body, response.statusCode())));
    }

    @Getter
    public static class GitHubApiException extends RuntimeException {
        private final HttpStatusCode statusCode;

        public GitHubApiException(String message, HttpStatusCode statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
    }
}
