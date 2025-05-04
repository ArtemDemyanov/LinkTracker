package backend.academy.bot.client;

import backend.academy.bot.config.BotConfig;
import backend.academy.dto.request.AddLinkRequest;
import backend.academy.dto.request.RemoveLinkRequest;
import backend.academy.dto.response.LinkResponse;
import backend.academy.dto.response.ListLinksResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ScrapperClient {
    private static final Logger logger = LoggerFactory.getLogger(ScrapperClient.class);
    private final WebClient webClient;
    private final Retry retry;

    public ScrapperClient(
            WebClient.Builder webClientBuilder,
            BotConfig botConfig,
            io.github.resilience4j.retry.RetryRegistry retryRegistry) {
        this.webClient = webClientBuilder.baseUrl(botConfig.scrapperUrl()).build();
        this.retry = retryRegistry.retry("scrapperClient");
    }

    @TimeLimiter(name = "scrapperClient")
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "fallbackVoid")
    public Mono<Void> registerChat(Long chatId) {
        return webClient
                .post()
                .uri("/Tg-Chat-Id/{id}", chatId)
                .retrieve()
                .bodyToMono(Void.class)
                .transformDeferred(RetryOperator.of(retry))
                .doOnSubscribe(s -> logger.info("Регистрация чата chatId: {}", chatId));
    }

    @TimeLimiter(name = "scrapperClient")
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "fallbackLinkResponse")
    public Mono<LinkResponse> addLink(Long chatId, LinkResponse link) {
        return webClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", chatId.toString())
                .bodyValue(new AddLinkRequest(link.url(), link.tags(), link.filters()))
                .retrieve()
                .bodyToMono(LinkResponse.class)
                .transformDeferred(RetryOperator.of(retry))
                .doOnSubscribe(s -> logger.info("Добавление ссылки chatId: {}, url: {}", chatId, link.url()));
    }

    @TimeLimiter(name = "scrapperClient")
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "fallbackVoid")
    public Mono<Void> removeLink(Long chatId, URI urlToRemove) {
        RemoveLinkRequest request = new RemoveLinkRequest(urlToRemove);

        return webClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", chatId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .transformDeferred(RetryOperator.of(retry))
                .doOnSubscribe(s -> logger.info("Удаление ссылки chatId: {}, url: {}", chatId, urlToRemove))
                .doOnError(e -> logger.error("Ошибка при удалении ссылки chatId: {}, url: {}", chatId, urlToRemove, e));
    }

    @TimeLimiter(name = "scrapperClient")
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "fallbackLinkList")
    public Mono<List<LinkResponse>> getLinks(Long chatId) {
        return webClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", chatId.toString())
                .retrieve()
                .bodyToMono(ListLinksResponse.class)
                .map(ListLinksResponse::links)
                .defaultIfEmpty(Collections.emptyList())
                .transformDeferred(RetryOperator.of(retry))
                .doOnSubscribe(s -> logger.info("Получение списка ссылок chatId: {}", chatId));
    }

    @TimeLimiter(name = "scrapperClient")
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "fallbackStringSet")
    public Mono<Set<String>> getAllTags(Long chatId) {
        return webClient
                .get()
                .uri("/tags")
                .header("Tg-Chat-Id", chatId.toString())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
                .defaultIfEmpty(Collections.emptySet())
                .transformDeferred(RetryOperator.of(retry))
                .doOnSubscribe(s -> logger.info("Получение всех тегов chatId: {}", chatId));
    }

    @TimeLimiter(name = "scrapperClient")
    @CircuitBreaker(name = "scrapperClient", fallbackMethod = "fallbackLinkListWithTags")
    public Mono<List<LinkResponse>> getLinksByTags(Long chatId, Set<String> tags) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/links/tags")
                        .queryParam("tags", tags.toArray())
                        .build())
                .header("Tg-Chat-Id", chatId.toString())
                .retrieve()
                .bodyToMono(ListLinksResponse.class)
                .map(ListLinksResponse::links)
                .defaultIfEmpty(Collections.emptyList())
                .transformDeferred(RetryOperator.of(retry))
                .doOnSubscribe(s -> logger.info("Получение ссылок по тегам chatId: {}, tags: {}", chatId, tags));
    }

    // Fallback-методы

    @SuppressWarnings("unused")
    private Mono<Void> fallbackVoid(Long chatId, Throwable throwable) {
        logger.warn(
                "Fallback triggered for void-returning method, chatId: {}, error: {}", chatId, throwable.getMessage());
        return Mono.empty();
    }

    @SuppressWarnings("unused")
    private Mono<LinkResponse> fallbackLinkResponse(Long chatId, LinkResponse link, Throwable throwable) {
        logger.warn("Fallback for addLink chatId: {}, url: {}, error: {}", chatId, link.url(), throwable.getMessage());
        return Mono.error(throwable);
    }

    @SuppressWarnings("unused")
    private Mono<List<LinkResponse>> fallbackLinkList(Long chatId, Throwable throwable) {
        logger.warn("Fallback for getLinks chatId: {}, error: {}", chatId, throwable.getMessage());
        return Mono.just(Collections.emptyList());
    }

    @SuppressWarnings("unused")
    private Mono<List<LinkResponse>> fallbackLinkListWithTags(Long chatId, Set<String> tags, Throwable throwable) {
        logger.warn(
                "Fallback for getLinksByTags chatId: {}, tags: {}, error: {}", chatId, tags, throwable.getMessage());
        return Mono.just(Collections.emptyList());
    }

    @SuppressWarnings("unused")
    private Mono<Set<String>> fallbackStringSet(Long chatId, Throwable throwable) {
        logger.warn("Fallback for getAllTags chatId: {}, error: {}", chatId, throwable.getMessage());
        return Mono.just(Collections.emptySet());
    }
}
