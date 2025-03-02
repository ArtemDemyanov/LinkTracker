package backend.academy.bot.state;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Машина состояний для управления состоянием пользователя. Этот класс отслеживает текущее состояние пользователя и
 * данные, связанные с этим состоянием.
 */
@Component
public class StateMachine {

    private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);
    private final Map<Long, TrackState> states = new HashMap<>();
    private final Map<Long, URI> pendingLinks = new HashMap<>();
    private final Map<Long, List<String>> pendingTags = new HashMap<>();
    private final Map<Long, List<String>> pendingFilters = new HashMap<>();

    /**
     * Возвращает текущее состояние пользователя.
     *
     * @param chatId Уникальный идентификатор чата.
     * @return Текущее состояние пользователя.
     */
    public TrackState getState(Long chatId) {
        TrackState state = states.getOrDefault(chatId, TrackState.IDLE);
        logger.atDebug()
                .setMessage("Retrieved state for chat")
                .addKeyValue("chatId", chatId)
                .addKeyValue("state", state)
                .log();
        return state;
    }

    /**
     * Устанавливает текущее состояние пользователя.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param state Новое состояние пользователя.
     */
    public void setState(Long chatId, TrackState state) {
        logger.atInfo()
                .setMessage("Setting state for chat")
                .addKeyValue("chatId", chatId)
                .addKeyValue("state", state)
                .log();
        states.put(chatId, state);
    }

    /**
     * Устанавливает ожидаемую ссылку для пользователя.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param link Ссылка, ожидаемая от пользователя.
     */
    public void setPendingLink(Long chatId, URI link) {
        logger.atInfo()
                .setMessage("Setting pending link for chat")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", link)
                .log();
        pendingLinks.put(chatId, link);
    }

    /**
     * Возвращает ожидаемую ссылку для пользователя.
     *
     * @param chatId Уникальный идентификатор чата.
     * @return Ожидаемая ссылка или null, если ссылка не установлена.
     */
    public URI getPendingLink(Long chatId) {
        URI link = pendingLinks.get(chatId);
        logger.atDebug()
                .setMessage("Retrieved pending link for chat")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", link)
                .log();
        return link;
    }

    /**
     * Устанавливает ожидаемые теги для пользователя.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param tags Список тегов, ожидаемых от пользователя.
     */
    public void setPendingTags(Long chatId, List<String> tags) {
        logger.atInfo()
                .setMessage("Setting pending tags for chat")
                .addKeyValue("chatId", chatId)
                .addKeyValue("tags", tags)
                .log();
        pendingTags.put(chatId, tags);
    }

    /**
     * Возвращает ожидаемые теги для пользователя.
     *
     * @param chatId Уникальный идентификатор чата.
     * @return Список ожидаемых тегов или пустой список, если теги не установлены.
     */
    public List<String> getPendingTags(Long chatId) {
        List<String> tags = pendingTags.getOrDefault(chatId, Collections.emptyList());
        logger.atDebug()
                .setMessage("Retrieved pending tags for chat")
                .addKeyValue("chatId", chatId)
                .addKeyValue("tags", tags)
                .log();
        return tags;
    }

    /**
     * Устанавливает ожидаемые фильтры для пользователя.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param filters Список фильтров, ожидаемых от пользователя.
     */
    public void setPendingFilters(Long chatId, List<String> filters) {
        logger.atInfo()
                .setMessage("Setting pending filters for chat")
                .addKeyValue("chatId", chatId)
                .addKeyValue("filters", filters)
                .log();
        pendingFilters.put(chatId, filters);
    }

    /**
     * Возвращает ожидаемые фильтры для пользователя.
     *
     * @param chatId Уникальный идентификатор чата.
     * @return Список ожидаемых фильтров или пустой список, если фильтры не установлены.
     */
    public List<String> getPendingFilters(Long chatId) {
        List<String> filters = pendingFilters.getOrDefault(chatId, Collections.emptyList());
        logger.atDebug()
                .setMessage("Retrieved pending filters for chat")
                .addKeyValue("chatId", chatId)
                .addKeyValue("filters", filters)
                .log();
        return filters;
    }

    /**
     * Очищает все ожидаемые данные для пользователя.
     *
     * @param chatId Уникальный идентификатор чата.
     */
    public void clearPendingData(Long chatId) {
        logger.atInfo()
                .setMessage("Clearing pending data for chat")
                .addKeyValue("chatId", chatId)
                .log();
        pendingLinks.remove(chatId);
        pendingTags.remove(chatId);
        pendingFilters.remove(chatId);
    }
}
