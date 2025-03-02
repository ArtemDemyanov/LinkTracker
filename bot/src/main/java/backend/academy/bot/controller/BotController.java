package backend.academy.bot.controller;

import backend.academy.bot.controller.response.ApiErrorResponse;
import backend.academy.bot.service.BotService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для обработки запросов Telegram бота. Этот класс обрабатывает входящие сообщения и команды от
 * пользователей.
 */
@RestController
@RequestMapping("/")
public class BotController {

    private static final Logger logger = LoggerFactory.getLogger(BotController.class);
    private final TelegramBot bot;
    private final BotService botService;

    /**
     * Конструктор класса BotController. Инициализирует Telegram бота и сервис для обработки сообщений.
     *
     * @param bot Экземпляр Telegram бота.
     * @param botService Сервис для обработки сообщений и команд.
     */
    public BotController(TelegramBot bot, BotService botService) {
        this.bot = bot;
        this.botService = botService;

        bot.setUpdatesListener(updates -> {
            updates.forEach(this::processUpdate);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    /**
     * Обрабатывает входящее обновление от Telegram.
     *
     * @param update Объект обновления, содержащий информацию о сообщении.
     */
    private void processUpdate(Update update) {
        if (update.message() != null && update.message().text() != null) {
            Long chatId = update.message().chat().id();
            String messageText = update.message().text();

            logger.atInfo()
                    .setMessage("Processing update")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("messageText", messageText)
                    .log();

            String response = botService.handleMessage(chatId, messageText);
            bot.execute(new SendMessage(chatId, response));
        }
    }

    /**
     * Обрабатывает уведомления об обновлениях ссылок.
     *
     * @param linkUpdate Объект, содержащий информацию об обновлении ссылки.
     * @return Ответ с кодом состояния HTTP 200, если уведомления успешно отправлены.
     */
    @Operation(summary = "Отправить обновление")
    @ApiResponse(
            responseCode = "200",
            description = "Обновление обработано",
            content = @Content(schema = @Schema(implementation = LinkUpdate.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping(value = "/links", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateNotify(@RequestBody LinkUpdate linkUpdate) {
        logger.atInfo()
                .setMessage("Sending update notifications")
                .addKeyValue("linkUpdate", linkUpdate)
                .log();

        for (Long chatId : linkUpdate.tgChatIds()) {
            SendMessage message = new SendMessage(chatId, linkUpdate.description());
            bot.execute(message);
        }
        return ResponseEntity.ok().build();
    }
}
