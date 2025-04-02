package backend.academy.bot.controller;

import backend.academy.bot.controller.response.ApiErrorResponse;
import com.pengrad.telegrambot.TelegramBot;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BotController {

    private static final Logger logger = LoggerFactory.getLogger(BotController.class);
    private final TelegramBot bot;

    /**
     * Контроллер для обработки запросов, связанных с Telegram ботом.
     *
     * @param bot Экземпляр Telegram бота.
     */
    public BotController(TelegramBot bot) {
        this.bot = bot;
    }

    /**
     * Отправляет уведомления об обновлении ссылки пользователям Telegram.
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
