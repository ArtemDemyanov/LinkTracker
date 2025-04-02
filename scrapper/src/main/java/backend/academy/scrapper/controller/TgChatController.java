package backend.academy.scrapper.controller;

import backend.academy.scrapper.controller.response.ApiErrorResponse;
import backend.academy.scrapper.service.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TgChatController {
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    private final LinkService linkService;

    /**
     * Sanitizes input to prevent CRLF injection.
     *
     * @param input The input string to sanitize.
     * @return The sanitized string.
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        // Remove CRLF characters
        return input.replaceAll("[\r\n]", "");
    }

    /**
     * Регистрирует новый чат Telegram.
     *
     * @param id Идентификатор чата Telegram.
     */
    @Operation(summary = "Зарегистрировать чат")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Чат зарегистрирован"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @PostMapping("/" + TG_CHAT_ID_HEADER + "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void registerChat(@PathVariable Long id) {
        // Sanitize the ID before logging
        String sanitizedId = sanitizeInput(id.toString());
        log.debug("Registering chat: {}", sanitizedId);
        linkService.registerChat(id);
    }

    /**
     * Удаляет существующий чат Telegram.
     *
     * @param id Идентификатор чата Telegram.
     */
    @Operation(summary = "Удалить чат")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Чат успешно удалён"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Чат не существует",
                        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @DeleteMapping("/" + TG_CHAT_ID_HEADER + "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteChat(@PathVariable Long id) {
        // Sanitize the ID before logging
        String sanitizedId = sanitizeInput(id.toString());
        log.debug("Deleting chat: {}", sanitizedId);
        linkService.deleteChat(id);
    }
}
