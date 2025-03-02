package backend.academy.bot.commands.impl;

import static backend.academy.bot.message.BotCommandMessage.LIST_OF_REF_IS_EMPTY;
import static backend.academy.bot.message.BotCommandMessage.TRACKED_REF;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.commands.BotCommandHandler;
import backend.academy.bot.model.TrackedLink;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Обработчик команды /list. Этот класс обрабатывает команду /list, возвращая пользователю список отслеживаемых ссылок.
 */
@Component
public class ListCommandHandler implements BotCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListCommandHandler.class);
    private final ScrapperClient scrapperClient;

    /**
     * Конструктор класса ListCommandHandler. Инициализирует объект ScrapperClient для взаимодействия с сервисом
     * Scrapper.
     *
     * @param scrapperClient Клиент для взаимодействия с сервисом Scrapper.
     */
    public ListCommandHandler(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    /**
     * Обрабатывает команду /list. Этот метод возвращает пользователю список отслеживаемых ссылок или сообщение о том,
     * что список пуст.
     *
     * @param chatId Уникальный идентификатор чата, в котором была отправлена команда.
     * @param message Текст сообщения, содержащий команду /list.
     * @return Строка с перечнем отслеживаемых ссылок или сообщение о пустом списке.
     */
    @Override
    public String handle(Long chatId, String message) {
        logger.atInfo()
                .setMessage("Handling /list command")
                .addKeyValue("chatId", chatId)
                .log();

        List<TrackedLink> trackedLinks = scrapperClient.getLinks(chatId);

        if (trackedLinks.isEmpty()) {
            logger.atDebug()
                    .setMessage("No tracked links found")
                    .addKeyValue("chatId", chatId)
                    .log();
            return LIST_OF_REF_IS_EMPTY;
        }

        StringBuilder result = new StringBuilder(TRACKED_REF);

        for (TrackedLink link : trackedLinks) {
            result.append(link.url().toString());

            if (!link.tags().isEmpty()) {
                result.append(" | Tags: ").append(String.join(", ", link.tags()));
            }

            if (!link.filters().isEmpty()) {
                result.append(" | Filters: ").append(String.join(", ", link.filters()));
            }

            result.append("\n");
        }

        logger.atDebug()
                .setMessage("Tracked links retrieved")
                .addKeyValue("chatId", chatId)
                .addKeyValue("trackedLinksCount", trackedLinks.size())
                .log();

        return result.toString().trim();
    }
}
