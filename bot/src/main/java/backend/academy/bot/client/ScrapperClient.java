package backend.academy.bot.client;

import backend.academy.bot.client.request.AddLinkRequest;
import backend.academy.bot.client.request.RemoveLinkRequest;
import backend.academy.bot.client.response.LinkResponse;
import backend.academy.bot.client.response.ListLinksResponse;
import backend.academy.bot.config.BotConfig;
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

    /**
     * Конструктор клиента Scrapper.
     *
     * @param webClientBuilder билдер для создания WebClient
     * @param botConfig конфигурация бота, содержащая базовый URL Scrapper
     */
    public ScrapperClient(WebClient.Builder webClientBuilder, BotConfig botConfig) {
        this.webClient = webClientBuilder.baseUrl(botConfig.scrapperUrl()).build();
    }

    /**
     * Регистрирует чат в Scrapper.
     *
     * @param chatId идентификатор чата для регистрации
     * @return Mono<Void>, завершающийся при успешной регистрации
     */
    public Mono<Void> registerChat(Long chatId) {
        return webClient
                .post()
                .uri("/Tg-Chat-Id/{id}", chatId)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSubscribe(s -> logger.info("Регистрация чата chatId: {}", chatId));
    }

    /**
     * Добавляет ссылку для отслеживания в указанном чате.
     *
     * @param chatId идентификатор чата, в который добавляется ссылка
     * @param link данные ссылки для добавления (URL, теги, фильтры)
     * @return Mono<LinkResponse> с информацией о добавленной ссылке
     */
    public Mono<LinkResponse> addLink(Long chatId, LinkResponse link) {
        return webClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", chatId.toString())
                .bodyValue(new AddLinkRequest(link.url(), link.tags(), link.filters()))
                .retrieve()
                .bodyToMono(LinkResponse.class)
                .doOnSubscribe(s -> logger.info("Добавление ссылки chatId: {}, url: {}", chatId, link.url()));
    }

    /**
     * Удаляет ссылку из отслеживания в указанном чате.
     *
     * @param chatId идентификатор чата, из которого удаляется ссылка
     * @param urlToRemove URL ссылки для удаления
     * @return Mono<Void>, завершающийся при успешном удалении
     */
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
                .doOnSubscribe(s -> logger.info("Удаление ссылки chatId: {}, url: {}", chatId, urlToRemove))
                .doOnError(e -> logger.error("Ошибка при удалении ссылки chatId: {}, url: {}", chatId, urlToRemove, e));
    }

    /**
     * Получает все отслеживаемые ссылки для указанного чата.
     *
     * @param chatId идентификатор чата
     * @return Mono<List<LinkResponse>> со списком всех ссылок чата
     */
    public Mono<List<LinkResponse>> getLinks(Long chatId) {
        return webClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", chatId.toString())
                .retrieve()
                .bodyToMono(ListLinksResponse.class)
                .map(ListLinksResponse::links)
                .defaultIfEmpty(Collections.emptyList())
                .doOnSubscribe(s -> logger.info("Получение списка ссылок chatId: {}", chatId));
    }

    /**
     * Получает все теги для указанного чата.
     *
     * @param chatId идентификатор чата
     * @return Mono<Set<String>> с набором всех тегов чата
     */
    public Mono<Set<String>> getAllTags(Long chatId) {
        return webClient
                .get()
                .uri("/tags")
                .header("Tg-Chat-Id", chatId.toString())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Set<String>>() {})
                .defaultIfEmpty(Collections.emptySet())
                .doOnSubscribe(s -> logger.info("Получение всех тегов chatId: {}", chatId));
    }

    /**
     * Получает ссылки по указанным тегам для заданного чата.
     *
     * @param chatId идентификатор чата
     * @param tags набор тегов для фильтрации ссылок
     * @return Mono<List<LinkResponse>> со списком ссылок, соответствующих тегам
     */
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
                .doOnSubscribe(s -> logger.info("Получение ссылок по тегам chatId: {}, tags: {}", chatId, tags));
    }
}
