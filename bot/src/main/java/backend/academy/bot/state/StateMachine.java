package backend.academy.bot.state;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StateMachine {

    private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);
    private final Map<Long, TrackState> states = new HashMap<>();
    private final Map<Long, URI> pendingLinks = new HashMap<>();
    private final Map<Long, List<String>> pendingTags = new HashMap<>();
    private final Map<Long, List<String>> pendingFilters = new HashMap<>();

    /**
     * Возвращает текущее состояние чата.
     *
     * @param chatId Идентификатор чата.
     * @return Текущее состояние чата. Если состояние не установлено, возвращается {@link TrackState#IDLE}.
     */
    public TrackState getState(Long chatId) {
        TrackState state = states.getOrDefault(chatId, TrackState.IDLE);
        logger.atDebug()
                .setMessage("Получение состояния")
                .addKeyValue("chatId", chatId)
                .addKeyValue("state", state)
                .log();
        return state;
    }

    /**
     * Устанавливает состояние чата.
     *
     * @param chatId Идентификатор чата.
     * @param state Новое состояние чата.
     */
    public void setState(Long chatId, TrackState state) {
        logger.atInfo()
                .setMessage("Установка состояния")
                .addKeyValue("chatId", chatId)
                .addKeyValue("state", state)
                .log();
        states.put(chatId, state);
    }

    /**
     * Устанавливает ожидаемую ссылку для чата.
     *
     * @param chatId Идентификатор чата.
     * @param link Ссылка, которую необходимо сохранить.
     */
    public void setPendingLink(Long chatId, URI link) {
        logger.atInfo()
                .setMessage("Установка ожидаемой ссылки")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", link)
                .log();
        pendingLinks.put(chatId, link);
    }

    /**
     * Возвращает ожидаемую ссылку для чата.
     *
     * @param chatId Идентификатор чата.
     * @return Ожидаемая ссылка. Если ссылка не установлена, возвращается null.
     */
    public URI getPendingLink(Long chatId) {
        URI link = pendingLinks.get(chatId);
        logger.atDebug()
                .setMessage("Получение ожидаемой ссылки")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", link)
                .log();
        return link;
    }

    /**
     * Устанавливает ожидаемые теги для чата.
     *
     * @param chatId Идентификатор чата.
     * @param tags Список тегов, которые необходимо сохранить.
     */
    public void setPendingTags(Long chatId, List<String> tags) {
        logger.atInfo()
                .setMessage("Установка ожидаемых тегов")
                .addKeyValue("chatId", chatId)
                .addKeyValue("tags", tags)
                .log();
        pendingTags.put(chatId, tags);
    }

    /**
     * Возвращает ожидаемые теги для чата.
     *
     * @param chatId Идентификатор чата.
     * @return Список ожидаемых тегов. Если теги не установлены, возвращается пустой список.
     */
    public List<String> getPendingTags(Long chatId) {
        List<String> tags = pendingTags.getOrDefault(chatId, Collections.emptyList());
        logger.atDebug()
                .setMessage("Получение ожидаемых тегов")
                .addKeyValue("chatId", chatId)
                .addKeyValue("tags", tags)
                .log();
        return tags;
    }

    /**
     * Устанавливает ожидаемые фильтры для чата.
     *
     * @param chatId Идентификатор чата.
     * @param filters Список фильтров, которые необходимо сохранить.
     */
    public void setPendingFilters(Long chatId, List<String> filters) {
        logger.atInfo()
                .setMessage("Установка ожидаемых фильтров")
                .addKeyValue("chatId", chatId)
                .addKeyValue("filters", filters)
                .log();
        pendingFilters.put(chatId, filters);
    }

    /**
     * Возвращает ожидаемые фильтры для чата.
     *
     * @param chatId Идентификатор чата.
     * @return Список ожидаемых фильтров. Если фильтры не установлены, возвращается пустой список.
     */
    public List<String> getPendingFilters(Long chatId) {
        List<String> filters = pendingFilters.getOrDefault(chatId, Collections.emptyList());
        logger.atDebug()
                .setMessage("Получение ожидаемых фильтров")
                .addKeyValue("chatId", chatId)
                .addKeyValue("filters", filters)
                .log();
        return filters;
    }

    /**
     * Очищает все временные данные для чата.
     *
     * @param chatId Идентификатор чата.
     */
    public void clearPendingData(Long chatId) {
        logger.atInfo()
                .setMessage("Очистка ожидаемых данных")
                .addKeyValue("chatId", chatId)
                .log();
        pendingLinks.remove(chatId);
        pendingTags.remove(chatId);
        pendingFilters.remove(chatId);
    }
}
