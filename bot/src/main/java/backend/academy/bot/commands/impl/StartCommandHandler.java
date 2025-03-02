package backend.academy.bot.commands.impl;

import static backend.academy.bot.message.BotCommandMessage.WELCOME_NEW_USER;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.commands.BotCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Обработчик команды /start. Этот класс обрабатывает команду /start, регистрируя пользователя и отправляя
 * приветственное сообщение.
 */
@Component
public class StartCommandHandler implements BotCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(StartCommandHandler.class);
    private final ScrapperClient scrapperClient;

    /**
     * Конструктор класса StartCommandHandler. Инициализирует объект ScrapperClient для взаимодействия с сервисом
     * Scrapper.
     *
     * @param scrapperClient Клиент для взаимодействия с сервисом Scrapper.
     */
    public StartCommandHandler(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    /**
     * Обрабатывает команду /start. Этот метод регистрирует чат в сервисе Scrapper и отправляет пользователю
     * приветственное сообщение.
     *
     * @param chatId Уникальный идентификатор чата, в котором была отправлена команда.
     * @param message Текст сообщения, содержащий команду /start.
     * @return Приветственное сообщение для нового пользователя.
     */
    @Override
    public String handle(Long chatId, String message) {
        logger.atInfo()
                .setMessage("Handling /start command")
                .addKeyValue("chatId", chatId)
                .log();

        scrapperClient.registerChat(chatId);
        return WELCOME_NEW_USER;
    }
}
