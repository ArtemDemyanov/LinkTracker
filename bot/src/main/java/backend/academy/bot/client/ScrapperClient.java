package backend.academy.bot.client;

import backend.academy.bot.client.request.AddLinkRequest;
import backend.academy.bot.client.request.RemoveLinkRequest;
import backend.academy.bot.client.response.LinkResponse;
import backend.academy.bot.client.response.ListLinksResponse;
import backend.academy.bot.model.TrackedLink;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Клиент для взаимодействия с сервисом Scrapper. Этот класс предоставляет методы для регистрации чатов, добавления и
 * удаления ссылок, а также получения списка отслеживаемых ссылок для конкретного чата.
 */
@Component
public class ScrapperClient {

    private static final Logger logger = LoggerFactory.getLogger(ScrapperClient.class);
    private final WebClient webClient;

    /**
     * Конструктор класса ScrapperClient. Инициализирует WebClient с базовым URL сервиса Scrapper.
     *
     * @param webClientBuilder Builder для создания экземпляра WebClient.
     */
    public ScrapperClient(WebClient.Builder webClientBuilder) {
        String scrapperBaseUrl = "http://localhost:8081";
        this.webClient = webClientBuilder.baseUrl(scrapperBaseUrl).build();
    }

    /**
     * Регистрирует чат в сервисе Scrapper.
     *
     * @param chatId Уникальный идентификатор чата, который необходимо зарегистрировать.
     */
    public void registerChat(Long chatId) {
        String url = "/tg-chat/" + chatId;
        logger.atInfo()
                .setMessage("Registering chat")
                .addKeyValue("chatId", chatId)
                .log();
        webClient
                .post()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .block(); // Блокируем поток, так как метод синхронный
    }

    /**
     * Добавляет новую ссылку в сервис Scrapper для указанного чата.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param link Объект {@link TrackedLink}, содержащий данные о добавляемой ссылке (URL, теги, фильтры).
     */
    public void addLink(Long chatId, TrackedLink link) {
        String url = "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", chatId.toString());

        AddLinkRequest request = new AddLinkRequest(link.url(), link.tags(), link.filters());

        logger.atInfo()
                .setMessage("Adding link to scrapper")
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", link.url())
                .addKeyValue("tags", link.tags())
                .addKeyValue("filters", link.filters())
                .log();

        webClient
                .post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LinkResponse.class)
                .block(); // Блокируем поток, так как метод синхронный
    }

    /**
     * Удаляет ссылку из сервиса Scrapper для указанного чата.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param urlToRemove URI ссылки, которую необходимо удалить.
     */
    public void removeLink(Long chatId, URI urlToRemove) {
        String endpointUrl = "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", chatId.toString());

        RemoveLinkRequest request = new RemoveLinkRequest(urlToRemove);

        logger.atInfo()
                .setMessage("Removing link from scrapper")
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", urlToRemove)
                .log();

        webClient
                .method(HttpMethod.DELETE)
                .uri(endpointUrl)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LinkResponse.class)
                .block(); // Блокируем поток, так как метод синхронный
    }

    /**
     * Получает список отслеживаемых ссылок для указанного чата.
     *
     * @param chatId Уникальный идентификатор чата.
     * @return Список объектов {@link TrackedLink}, представляющих отслеживаемые ссылки. Если ссылок нет, возвращается
     *     пустой список.
     */
    public List<TrackedLink> getLinks(Long chatId) {
        String url = "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", chatId.toString());

        logger.atDebug()
                .setMessage("Fetching links for chat")
                .addKeyValue("chatId", chatId)
                .log();

        ListLinksResponse response = webClient
                .get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .retrieve()
                .bodyToMono(ListLinksResponse.class)
                .block(); // Блокируем поток, так как метод синхронный

        if (response == null || response.links() == null) {
            logger.atWarn()
                    .setMessage("No links found for chat")
                    .addKeyValue("chatId", chatId)
                    .log();
            return Collections.emptyList();
        }

        return response.links().stream()
                .map(linkResponse -> new TrackedLink(linkResponse.url(), linkResponse.tags(), linkResponse.filters()))
                .collect(Collectors.toList());
    }
}
