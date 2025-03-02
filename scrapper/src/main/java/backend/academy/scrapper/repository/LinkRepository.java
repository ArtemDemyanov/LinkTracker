package backend.academy.scrapper.repository;

import backend.academy.scrapper.controller.response.LinkResponse;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для управления отслеживаемыми ссылками. Этот класс предоставляет методы для добавления, удаления и
 * получения ссылок.
 */
@Repository
public class LinkRepository {

    private static final Logger logger = LoggerFactory.getLogger(LinkRepository.class);

    // Хранилище для отслеживаемых ссылок
    private final Map<Long, Set<LinkResponse>> trackedLinks = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // Отдельное хранилище для времени последнего обновления
    private final Map<Long, String> lastUpdatedMap = new HashMap<>();
    private final Map<Long, String> lastActivityDateMap = new HashMap<>();

    /**
     * Добавляет новую ссылку для чата.
     *
     * @param chatId ID чата.
     * @param link Ссылка для добавления.
     */
    public void addLink(Long chatId, LinkResponse link) {
        Set<LinkResponse> links = trackedLinks.computeIfAbsent(chatId, k -> new HashSet<>());

        boolean linkExists =
                links.stream().anyMatch(existingLink -> existingLink.url().equals(link.url()));

        if (!linkExists) {
            links.add(link);
            lastUpdatedMap.put(link.id(), null); // Инициализация времени последнего обновления
            logger.atInfo()
                    .setMessage("Link added to repository")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("linkId", link.id())
                    .addKeyValue("url", link.url())
                    .log();
        } else {
            logger.atDebug()
                    .setMessage("Link already exists in repository")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("url", link.url())
                    .log();
        }
    }

    /**
     * Удаляет ссылку из чата.
     *
     * @param chatId ID чата.
     * @param url URL ссылки для удаления.
     */
    public void removeLink(Long chatId, URI url) {
        Set<LinkResponse> links = trackedLinks.getOrDefault(chatId, Collections.emptySet());
        Optional<LinkResponse> linkToRemove =
                links.stream().filter(link -> link.url().equals(url)).findFirst();
        linkToRemove.ifPresent(link -> {
            links.remove(link);
            lastUpdatedMap.remove(link.id()); // Удаляем запись о времени обновления
            logger.atInfo()
                    .setMessage("Link removed from repository")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("linkId", link.id())
                    .addKeyValue("url", link.url())
                    .log();
        });
    }

    /**
     * Получает все ссылки для конкретного чата.
     *
     * @param chatId ID чата.
     * @return Множество ссылок.
     */
    public Set<LinkResponse> getLinks(Long chatId) {
        Set<LinkResponse> links = trackedLinks.getOrDefault(chatId, Collections.emptySet());
        logger.atDebug()
                .setMessage("Retrieved links for chat")
                .addKeyValue("chatId", chatId)
                .addKeyValue("linksCount", links.size())
                .log();
        return links;
    }

    /**
     * Получает все ссылки из всех чатов.
     *
     * @return Множество всех ссылок.
     */
    public Set<LinkResponse> getAllLinks() {
        Set<LinkResponse> allLinks =
                trackedLinks.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
        logger.atDebug()
                .setMessage("Retrieved all links from repository")
                .addKeyValue("totalLinksCount", allLinks.size())
                .log();
        return allLinks;
    }

    /**
     * Генерирует уникальный ID для новой ссылки.
     *
     * @return Уникальный ID.
     */
    public Long generateId() {
        long newId = idGenerator.getAndIncrement();
        logger.atDebug()
                .setMessage("Generated new link ID")
                .addKeyValue("newId", newId)
                .log();
        return newId;
    }

    /**
     * Получает время последнего обновления для ссылки.
     *
     * @param linkId ID ссылки.
     * @return Время последнего обновления (или null, если не найдено).
     */
    public String getLastUpdated(Long linkId) {
        String lastUpdated = lastUpdatedMap.get(linkId);
        logger.atDebug()
                .setMessage("Retrieved last updated time for link")
                .addKeyValue("linkId", linkId)
                .addKeyValue("lastUpdated", lastUpdated)
                .log();
        return lastUpdated;
    }

    /**
     * Обновляет время последнего обновления для ссылки.
     *
     * @param linkId ID ссылки.
     * @param lastUpdated Новое время последнего обновления.
     */
    public void updateLastUpdated(Long linkId, String lastUpdated) {
        lastUpdatedMap.put(linkId, lastUpdated);
        logger.atInfo()
                .setMessage("Updated last updated time for link")
                .addKeyValue("linkId", linkId)
                .addKeyValue("lastUpdated", lastUpdated)
                .log();
    }

    /**
     * Получает все chatId, связанные с конкретной ссылкой.
     *
     * @param linkId ID ссылки.
     * @return Список chatId.
     */
    public List<Long> getChatIdsByLinkId(Long linkId) {
        List<Long> chatIds = trackedLinks.entrySet().stream()
                .filter(entry ->
                        entry.getValue().stream().anyMatch(link -> link.id().equals(linkId)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        logger.atDebug()
                .setMessage("Retrieved chat IDs for link")
                .addKeyValue("linkId", linkId)
                .addKeyValue("chatIdsCount", chatIds.size())
                .log();
        return chatIds;
    }

    /**
     * Получает время последней активности для ссылки.
     *
     * @param linkId ID ссылки.
     * @return Время последней активности (или null, если не найдено).
     */
    public String getLastActivityDate(Long linkId) {
        String lastActivityDate = lastActivityDateMap.get(linkId);
        logger.atDebug()
                .setMessage("Retrieved last activity date for link")
                .addKeyValue("linkId", linkId)
                .addKeyValue("lastActivityDate", lastActivityDate)
                .log();
        return lastActivityDate;
    }

    /**
     * Обновляет время последней активности для ссылки.
     *
     * @param linkId ID ссылки.
     * @param lastActivityDate Новое время последней активности.
     */
    public void updateLastActivityDate(Long linkId, String lastActivityDate) {
        lastActivityDateMap.put(linkId, lastActivityDate);
        logger.atInfo()
                .setMessage("Updated last activity date for link")
                .addKeyValue("linkId", linkId)
                .addKeyValue("lastActivityDate", lastActivityDate)
                .log();
    }
}
