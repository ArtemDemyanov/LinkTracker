package backend.academy.bot.service;

import static backend.academy.bot.message.BotMessage.FILTERS;
import static backend.academy.bot.message.BotMessage.SUCCESS_ADD;
import static backend.academy.bot.message.BotMessage.SUCCESS_DELETE;
import static backend.academy.bot.message.BotMessage.TAGS;
import static backend.academy.bot.message.BotMessage.UNKNOWN_COMMAND;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.commands.BotCommandHandler;
import backend.academy.bot.commands.CommandHandlerFactory;
import backend.academy.bot.model.TrackedLink;
import backend.academy.bot.state.StateMachine;
import backend.academy.bot.state.TrackState;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Сервис для обработки сообщений и команд бота. Этот класс отвечает за обработку входящих сообщений и управление
 * состоянием пользователя.
 */
@Service
public class BotService {

    private static final Logger logger = LoggerFactory.getLogger(BotService.class);
    private final CommandHandlerFactory commandHandlerFactory;
    private final StateMachine stateMachine;
    private final ScrapperClient scrapperClient;

    /**
     * Конструктор класса BotService.
     *
     * @param commandHandlerFactory Фабрика для получения обработчиков команд.
     * @param stateMachine Машина состояний для управления состоянием пользователя.
     * @param scrapperClient Клиент для взаимодействия с сервисом Scrapper.
     */
    public BotService(
            CommandHandlerFactory commandHandlerFactory, StateMachine stateMachine, ScrapperClient scrapperClient) {
        this.commandHandlerFactory = commandHandlerFactory;
        this.stateMachine = stateMachine;
        this.scrapperClient = scrapperClient;
    }

    /**
     * Обрабатывает входящее сообщение от пользователя.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param message Текст сообщения.
     * @return Ответное сообщение для пользователя.
     */
    public String handleMessage(Long chatId, String message) {
        TrackState currentState = stateMachine.getState(chatId);

        logger.atDebug()
                .setMessage("Handling message")
                .addKeyValue("chatId", chatId)
                .addKeyValue("message", message)
                .addKeyValue("currentState", currentState)
                .log();

        if (currentState == TrackState.IDLE) {
            BotCommandHandler handler = commandHandlerFactory.getHandler(message);
            if (handler != null) {
                return handler.handle(chatId, message);
            } else {
                logger.atWarn()
                        .setMessage("Unknown command received")
                        .addKeyValue("chatId", chatId)
                        .addKeyValue("message", message)
                        .log();
                return UNKNOWN_COMMAND;
            }
        }

        switch (currentState) {
            case AWAITING_LINK:
                URI uri = URI.create(message);
                return handleAwaitingLinkState(chatId, uri);
            case AWAITING_TAGS:
                return handleAwaitingTagsState(chatId, message);
            case AWAITING_FILTERS:
                return handleAwaitingFiltersState(chatId, message);
            case AWAITING_UNTRACK_LINK:
                URI uri2 = URI.create(message);
                return handleAwaitingUntrackLinkState(chatId, uri2);
            default:
                return UNKNOWN_COMMAND;
        }
    }

    /**
     * Обрабатывает состояние ожидания ссылки.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param link Ссылка, введенная пользователем.
     * @return Сообщение с запросом на ввод тегов.
     */
    private String handleAwaitingLinkState(Long chatId, URI link) {
        stateMachine.setPendingLink(chatId, link);
        stateMachine.setState(chatId, TrackState.AWAITING_TAGS);

        logger.atInfo()
                .setMessage("Link received, awaiting tags")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", link)
                .log();

        return TAGS;
    }

    /**
     * Обрабатывает состояние ожидания тегов.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param message Текст сообщения с тегами.
     * @return Сообщение с запросом на ввод фильтров.
     */
    private String handleAwaitingTagsState(Long chatId, String message) {
        if ("Пропустить".equalsIgnoreCase(message.trim())) {
            stateMachine.setPendingTags(chatId, Collections.emptyList());
        } else {
            String[] tags = message.split("\\s+");
            stateMachine.setPendingTags(chatId, Arrays.asList(tags));
        }

        stateMachine.setState(chatId, TrackState.AWAITING_FILTERS);

        logger.atInfo()
                .setMessage("Tags received, awaiting filters")
                .addKeyValue("chatId", chatId)
                .addKeyValue("tags", stateMachine.getPendingTags(chatId))
                .log();

        return FILTERS;
    }

    /**
     * Обрабатывает состояние ожидания фильтров.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param message Текст сообщения с фильтрами.
     * @return Сообщение об успешном добавлении ссылки.
     */
    private String handleAwaitingFiltersState(Long chatId, String message) {
        if ("Пропустить".equalsIgnoreCase(message.trim())) {
            stateMachine.setPendingFilters(chatId, Collections.emptyList());
        } else {
            List<String> filters = parseFilters(message);
            stateMachine.setPendingFilters(chatId, filters);
        }

        URI link = stateMachine.getPendingLink(chatId);
        List<String> tags = stateMachine.getPendingTags(chatId);
        List<String> filters = stateMachine.getPendingFilters(chatId);

        logger.atInfo()
                .setMessage("Filters received, adding link to scrapper")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", link)
                .addKeyValue("tags", tags)
                .addKeyValue("filters", filters)
                .log();

        scrapperClient.addLink(chatId, new TrackedLink(link, new HashSet<>(tags), new HashSet<>(filters)));
        stateMachine.clearPendingData(chatId);
        stateMachine.setState(chatId, TrackState.IDLE);
        return SUCCESS_ADD;
    }

    /**
     * Обрабатывает состояние ожидания ссылки для удаления.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param link Ссылка, которую нужно удалить.
     * @return Сообщение об успешном удалении ссылки.
     */
    private String handleAwaitingUntrackLinkState(Long chatId, URI link) {
        logger.atInfo()
                .setMessage("Removing link from scrapper")
                .addKeyValue("chatId", chatId)
                .addKeyValue("link", link)
                .log();

        scrapperClient.removeLink(chatId, link);
        stateMachine.setState(chatId, TrackState.IDLE);
        return SUCCESS_DELETE;
    }

    /**
     * Парсит фильтры из строки сообщения.
     *
     * @param message Текст сообщения с фильтрами.
     * @return Список фильтров.
     */
    private List<String> parseFilters(String message) {
        if (message == null || message.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(message.split("\\s+"))
                .map(s -> s.split(":"))
                .filter(arr -> arr.length == 2)
                .map(arr -> arr[0] + ":" + arr[1])
                .collect(Collectors.toList());
    }
}
