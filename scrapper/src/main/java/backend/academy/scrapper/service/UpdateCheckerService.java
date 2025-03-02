package backend.academy.scrapper.service;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.controller.response.LinkResponse;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.request.LinkUpdateRequest;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Сервис для проверки обновлений ссылок. Этот класс периодически проверяет обновления ссылок и отправляет уведомления в
 * бот.
 */
@Service
public class UpdateCheckerService {

    private static final Logger logger = LoggerFactory.getLogger(UpdateCheckerService.class);
    private final LinkRepository linkRepository;
    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final WebClient webClient;

    /**
     * Конструктор класса UpdateCheckerService.
     *
     * @param linkRepository Репозиторий для управления ссылками.
     * @param gitHubClient Клиент для взаимодействия с API GitHub.
     * @param stackOverflowClient Клиент для взаимодействия с API Stack Overflow.
     * @param webClientBuilder Builder для создания экземпляра WebClient.
     */
    public UpdateCheckerService(
            LinkRepository linkRepository,
            GitHubClient gitHubClient,
            StackOverflowClient stackOverflowClient,
            WebClient.Builder webClientBuilder) {
        this.linkRepository = linkRepository;
        this.gitHubClient = gitHubClient;
        this.stackOverflowClient = stackOverflowClient;
        String botBaseUrl = "http://localhost:8080";
        this.webClient = webClientBuilder.baseUrl(botBaseUrl).build();
    }

    /** Периодически проверяет обновления ссылок. */
    @Scheduled(fixedRate = 20000) // Проверка каждую минуту
    public void checkForUpdates() {
        Set<LinkResponse> allLinks = linkRepository.getAllLinks();
        for (LinkResponse link : allLinks) {
            URI url = link.url();
            List<Long> chatIds = linkRepository.getChatIdsByLinkId(link.id());
            if ("github.com".equals(url.getHost())) {
                gitHubClient
                        .getLastUpdated(url)
                        .doOnSuccess(lastUpdated -> {
                            String storedLastUpdated = linkRepository.getLastUpdated(link.id());
                            if (!lastUpdated.equals(storedLastUpdated)) {
                                logger.atInfo()
                                        .setMessage("GitHub link updated")
                                        .addKeyValue("linkId", link.id())
                                        .addKeyValue("url", url)
                                        .addKeyValue("lastUpdated", lastUpdated)
                                        .addKeyValue("storedLastUpdated", storedLastUpdated)
                                        .log();
                                sendNotificationToBot(link, lastUpdated, chatIds);
                                linkRepository.updateLastUpdated(link.id(), lastUpdated);
                            }
                        })
                        .doOnError(error -> logger.atError()
                                .setMessage("Error fetching data from GitHub")
                                .addKeyValue("url", url)
                                .addKeyValue("error", error.getMessage())
                                .log())
                        .subscribe();
            } else if ("stackoverflow.com".equals(url.getHost())) {
                stackOverflowClient
                        .getLastActivityDate(url)
                        .doOnSuccess(lastActivityDate -> {
                            String storedLastActivityDate = linkRepository.getLastActivityDate(link.id());
                            if (!lastActivityDate.toString().equals(storedLastActivityDate)) {
                                Instant instant = Instant.ofEpochSecond(lastActivityDate);
                                LocalDateTime formattedLastActivityDate =
                                        LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
                                logger.atInfo()
                                        .setMessage("Stack Overflow link updated")
                                        .addKeyValue("linkId", link.id())
                                        .addKeyValue("url", url)
                                        .addKeyValue("lastActivityDate", formattedLastActivityDate)
                                        .addKeyValue("storedLastActivityDate", storedLastActivityDate)
                                        .log();
                                sendNotificationToBot(link, formattedLastActivityDate.toString(), chatIds);
                                linkRepository.updateLastActivityDate(link.id(), lastActivityDate.toString());
                            }
                        })
                        .doOnError(error -> logger.atError()
                                .setMessage("Error fetching data from Stack Overflow")
                                .addKeyValue("url", url)
                                .addKeyValue("error", error.getMessage())
                                .log())
                        .subscribe();
            }
        }
    }

    /**
     * Отправляет уведомление в бот об обновлении ссылки.
     *
     * @param link Ссылка, которая была обновлена.
     * @param updateInfo Информация об обновлении.
     * @param chatIds Список идентификаторов чатов, которые должны быть уведомлены.
     */
    private void sendNotificationToBot(LinkResponse link, String updateInfo, List<Long> chatIds) {
        String botUrl = "/links";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        LinkUpdateRequest request = new LinkUpdateRequest(
                link.id(), link.url(), "Ссылка " + link.url() + " обновлена: " + updateInfo, chatIds);

        webClient
                .post()
                .uri(botUrl)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .doOnError(error -> logger.atError()
                        .setMessage("Error sending notification to bot")
                        .addKeyValue("linkId", link.id())
                        .addKeyValue("url", link.url())
                        .addKeyValue("error", error.getMessage())
                        .log())
                .subscribe();
    }
}
