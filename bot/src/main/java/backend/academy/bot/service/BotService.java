package backend.academy.bot.service;

import static backend.academy.bot.message.BotCommandMessage.LIST_OF_REF_IS_EMPTY;
import static backend.academy.bot.message.BotCommandMessage.TRACKED_REF;
import static backend.academy.bot.message.BotMessage.FILTERS;
import static backend.academy.bot.message.BotMessage.SUCCESS_ADD;
import static backend.academy.bot.message.BotMessage.SUCCESS_DELETE;
import static backend.academy.bot.message.BotMessage.TAGS;
import static backend.academy.bot.message.BotMessage.UNKNOWN_COMMAND;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.client.response.LinkResponse;
import backend.academy.bot.commands.BotCommandHandler;
import backend.academy.bot.commands.CommandHandlerFactory;
import backend.academy.bot.state.StateMachine;
import backend.academy.bot.state.TrackState;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class BotService {

    private static final Logger logger = LoggerFactory.getLogger(BotService.class);

    private final CommandHandlerFactory commandHandlerFactory;
    private final StateMachine stateMachine;
    private final ScrapperClient scrapperClient;
    private final TelegramBot bot;

    /**
     * @param commandHandlerFactory Фабрика обработчиков команд, используемая для получения обработчиков команд.
     * @param stateMachine Машина состояний, управляющая состоянием чатов.
     * @param scrapperClient Клиент для взаимодействия с сервисом скраппинга ссылок.
     * @param bot Экземпляр Telegram-бота для отправки сообщений.
     */
    public BotService(
            CommandHandlerFactory commandHandlerFactory,
            StateMachine stateMachine,
            ScrapperClient scrapperClient,
            TelegramBot bot) {
        this.commandHandlerFactory = commandHandlerFactory;
        this.stateMachine = stateMachine;
        this.scrapperClient = scrapperClient;
        this.bot = bot;
        initTelegramListener();
    }

    private void initTelegramListener() {
        bot.setUpdatesListener(updates -> {
            updates.forEach(this::processTelegramUpdate);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    /**
     * Обрабатывает входящее обновление от Telegram.
     *
     * @param update Входящее обновление, содержащее сообщение от пользователя.
     */
    private void processTelegramUpdate(Update update) {
        if (update.message() != null && update.message().text() != null) {
            Long chatId = update.message().chat().id();
            String messageText = update.message().text();
            handleMessage(chatId, messageText)
                    .subscribe(
                            response -> sendTelegramResponse(chatId, response),
                            error -> handleTelegramError(chatId, error));
        }
    }

    /**
     * Отправляет ответное сообщение пользователю через Telegram-бота.
     *
     * @param chatId Идентификатор чата, в который отправляется сообщение.
     * @param response Текст сообщения для отправки.
     */
    private void sendTelegramResponse(Long chatId, String response) {
        bot.execute(new SendMessage(chatId, response));
    }

    /**
     * Обрабатывает ошибки, возникающие при обработке сообщений.
     *
     * @param chatId Идентификатор чата, в котором произошла ошибка.
     * @param error Исключение, вызвавшее ошибку.
     */
    private void handleTelegramError(Long chatId, Throwable error) {
        logger.error("Error processing telegram message", error);
        bot.execute(new SendMessage(chatId, "Произошла ошибка при обработке сообщения"));
    }

    /**
     * Обрабатывает входящее сообщение от пользователя.
     *
     * @param chatId Идентификатор чата, из которого пришло сообщение.
     * @param message Текст сообщения от пользователя.
     * @return Mono<String> Ответное сообщение для пользователя.
     */
    public Mono<String> handleMessage(Long chatId, String message) {
        logger.info("Обработка сообщения chatId: {}, message: {}", chatId, message);
        TrackState currentState = stateMachine.getState(chatId);
        if (currentState == TrackState.IDLE) {
            BotCommandHandler handler = commandHandlerFactory.getHandler(message);
            if (handler != null) {
                return Mono.just(handler.handle(chatId, message));
            } else {
                logger.warn("Неизвестная команда chatId: {}, message: {}", chatId, message);
                return Mono.just(UNKNOWN_COMMAND);
            }
        }
        return switch (currentState) {
            case AWAITING_LINK -> handleAwaitingLinkState(chatId, message);
            case AWAITING_TAGS -> handleAwaitingTagsState(chatId, message);
            case AWAITING_FILTERS -> handleAwaitingFiltersState(chatId, message);
            case AWAITING_UNTRACK_LINK -> handleAwaitingUntrackLinkState(chatId, message);
            case AWAITING_TAG_DECISION -> handleAwaitingDecisionState(chatId, message);
            case AWAITING_TAGS_INPUT -> handleAwaitingTagsInputState(chatId, message);
            default -> {
                logger.warn("Неизвестное состояние chatId: {}, state: {}", chatId, currentState);
                yield Mono.just(UNKNOWN_COMMAND);
            }
        };
    }

    /**
     * Обрабатывает состояние ожидания ссылки от пользователя.
     *
     * @param chatId Идентификатор чата.
     * @param message Сообщение от пользователя, содержащее ссылку.
     * @return Mono<String> Ответное сообщение для пользователя.
     */
    private Mono<String> handleAwaitingLinkState(Long chatId, String message) {
        try {
            URI link = URI.create(message);
            logger.info("Ожидание ссылки chatId: {}, link: {}", chatId, link);
            stateMachine.setPendingLink(chatId, link);
            stateMachine.setState(chatId, TrackState.AWAITING_TAGS);
            return Mono.just(TAGS);
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректный URL chatId: {}, message: {}", chatId, message);
            return Mono.just("Некорректный URL. Пожалуйста, введите действительную ссылку.");
        }
    }

    /**
     * Обрабатывает состояние ожидания тегов от пользователя.
     *
     * @param chatId Идентификатор чата.
     * @param message Сообщение от пользователя, содержащее теги.
     * @return Mono<String> Ответное сообщение для пользователя.
     */
    private Mono<String> handleAwaitingTagsState(Long chatId, String message) {
        logger.info("Ожидание тегов chatId: {}, message: {}", chatId, message);
        if ("Пропустить".equalsIgnoreCase(message.trim())) {
            stateMachine.setPendingTags(chatId, Collections.emptyList());
        } else {
            String[] tags = message.split("\\s+");
            stateMachine.setPendingTags(chatId, Arrays.asList(tags));
        }
        stateMachine.setState(chatId, TrackState.AWAITING_FILTERS);
        return Mono.just(FILTERS);
    }

    /**
     * Обрабатывает состояние ожидания фильтров от пользователя.
     *
     * @param chatId Идентификатор чата.
     * @param message Сообщение от пользователя, содержащее фильтры.
     * @return Mono<String> Ответное сообщение для пользователя.
     */
    private Mono<String> handleAwaitingFiltersState(Long chatId, String message) {
        logger.info("Ожидание фильтров chatId: {}, message: {}", chatId, message);
        if ("Пропустить".equalsIgnoreCase(message.trim())) {
            stateMachine.setPendingFilters(chatId, Collections.emptyList());
        } else {
            List<String> filters = parseFilters(message);
            stateMachine.setPendingFilters(chatId, filters);
        }
        URI link = stateMachine.getPendingLink(chatId);
        List<String> tags = stateMachine.getPendingTags(chatId);
        List<String> filters = stateMachine.getPendingFilters(chatId);
        return scrapperClient
                .addLink(
                        chatId,
                        new LinkResponse(
                                null, // ID будет сгенерирован на сервере
                                link,
                                new HashSet<>(tags),
                                new HashSet<>(filters)))
                .then(Mono.fromCallable(() -> {
                    stateMachine.clearPendingData(chatId);
                    stateMachine.setState(chatId, TrackState.IDLE);
                    return SUCCESS_ADD;
                }));
    }

    /**
     * Обрабатывает состояние ожидания ссылки для удаления.
     *
     * @param chatId Идентификатор чата.
     * @param message Сообщение от пользователя, содержащее ссылку для удаления.
     * @return Mono<String> Ответное сообщение для пользователя.
     */
    private Mono<String> handleAwaitingUntrackLinkState(Long chatId, String message) {
        try {
            URI link = URI.create(message);
            logger.info("Удаление ссылки chatId: {}, link: {}", chatId, link);
            return scrapperClient.removeLink(chatId, link).then(Mono.fromCallable(() -> {
                stateMachine.setState(chatId, TrackState.IDLE);
                return SUCCESS_DELETE;
            }));
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректный URL chatId: {}, message: {}", chatId, message);
            return Mono.just("Некорректный URL. Пожалуйста, введите действительную ссылку.");
        }
    }

    /**
     * Парсит фильтры из сообщения пользователя.
     *
     * @param message Сообщение от пользователя, содержащее фильтры.
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

    /**
     * Обрабатывает состояние ожидания решения пользователя по тегам.
     *
     * @param chatId Идентификатор чата.
     * @param message Сообщение от пользователя с решением.
     * @return Mono<String> Ответное сообщение для пользователя.
     */
    private Mono<String> handleAwaitingDecisionState(Long chatId, String message) {
        if ("нет".equalsIgnoreCase(message)) {
            stateMachine.setState(chatId, TrackState.IDLE);
            return sendAllLinks(chatId);
        } else if ("да".equalsIgnoreCase(message)) {
            return scrapperClient.getAllTags(chatId).flatMap(userTags -> {
                if (userTags.isEmpty()) {
                    stateMachine.setState(chatId, TrackState.IDLE);
                    return Mono.just("У вас нет ссылок с тегами. Будут показаны все ссылки.\n")
                            .flatMap(msg -> sendAllLinks(chatId).map(msg2 -> msg + msg2));
                } else {
                    stateMachine.setState(chatId, TrackState.AWAITING_TAGS_INPUT);
                    return Mono.just("Доступные теги: " + String.join(", ", userTags)
                            + "\nВведите теги через запятую (например: работа,проект)");
                }
            });
        } else {
            return Mono.just("Пожалуйста, ответьте 'да' или 'нет'");
        }
    }

    /**
     * Обрабатывает состояние ожидания ввода тегов от пользователя.
     *
     * @param chatId Идентификатор чата.
     * @param message Сообщение от пользователя, содержащее теги.
     * @return Mono<String> Ответное сообщение для пользователя.
     */
    private Mono<String> handleAwaitingTagsInputState(Long chatId, String message) {
        Set<String> inputTags = Arrays.stream(message.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());
        if (inputTags.isEmpty()) {
            return Mono.just("Не указаны теги. Попробуйте еще раз");
        }
        return scrapperClient.getLinksByTags(chatId, inputTags).flatMap(filteredLinks -> {
            stateMachine.setState(chatId, TrackState.IDLE);
            if (filteredLinks.isEmpty()) {
                return Mono.just("Нет ссылок с указанными тегами.\n")
                        .flatMap(msg -> sendAllLinks(chatId).map(msg2 -> msg + msg2));
            }
            return Mono.just(formatFilteredLinks(filteredLinks));
        });
    }

    /**
     * Форматирует список ссылок с тегами и фильтрами для вывода пользователю.
     *
     * @param links Список ссылок для форматирования.
     * @return Отформатированная строка с информацией о ссылках.
     */
    private String formatFilteredLinks(List<LinkResponse> links) {
        StringBuilder result = new StringBuilder("Ссылки по выбранным тегам:\n");
        for (LinkResponse link : links) {
            result.append(link.url().toString());
            if (!link.tags().isEmpty()) {
                result.append(" | Теги: ").append(String.join(", ", link.tags()));
            }
            if (!link.filters().isEmpty()) {
                result.append(" | Фильтры: ").append(String.join(", ", link.filters()));
            }
            result.append("\n");
        }
        return result.toString().trim();
    }

    /**
     * Отправляет пользователю список всех отслеживаемых ссылок.
     *
     * @param chatId Идентификатор чата.
     * @return Mono<String> Ответное сообщение для пользователя.
     */
    private Mono<String> sendAllLinks(Long chatId) {
        return scrapperClient.getLinks(chatId).map(trackedLinks -> {
            if (trackedLinks.isEmpty()) {
                logger.info("Список ссылок пуст chatId: {}", chatId);
                return LIST_OF_REF_IS_EMPTY;
            }
            StringBuilder result = new StringBuilder(TRACKED_REF);
            for (LinkResponse link : trackedLinks) {
                result.append(link.url().toString());
                if (!link.tags().isEmpty()) {
                    result.append(" | Tags: ").append(String.join(", ", link.tags()));
                }
                if (!link.filters().isEmpty()) {
                    result.append(" | Filters: ").append(String.join(", ", link.filters()));
                }
                result.append("\n");
            }
            logger.info("Список ссылок успешно получен chatId: {}, links: {}", chatId, trackedLinks.size());
            return result.toString().trim();
        });
    }
}
