package backend.academy.scrapper.controller;

import backend.academy.scrapper.controller.request.AddLinkRequest;
import backend.academy.scrapper.controller.request.RemoveLinkRequest;
import backend.academy.scrapper.controller.response.ApiErrorResponse;
import backend.academy.scrapper.controller.response.LinkResponse;
import backend.academy.scrapper.controller.response.ListLinksResponse;
import backend.academy.scrapper.service.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LinksController {
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
     * Получает все отслеживаемые ссылки для указанного чата.
     *
     * @param chatId Идентификатор чата (передается в заголовке запроса).
     * @return ListLinksResponse Список отслеживаемых ссылок.
     */
    @Operation(summary = "Получить все отслеживаемые ссылки")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Ссылки успешно получены",
                        content = @Content(schema = @Schema(implementation = ListLinksResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @GetMapping("/links")
    public ListLinksResponse getLinks(@RequestHeader(TG_CHAT_ID_HEADER) Long chatId) {
        String sanitizedChatId = sanitizeInput(chatId.toString());
        log.debug("Getting links for chat: {}", sanitizedChatId);
        Set<LinkResponse> links = linkService.getLinks(chatId);
        return new ListLinksResponse(List.copyOf(links), links.size());
    }

    /**
     * Добавляет новую отслеживаемую ссылку для указанного чата.
     *
     * @param chatId Идентификатор чата (передается в заголовке запроса).
     * @param request Запрос на добавление ссылки, содержащий URL, теги и фильтры.
     * @return LinkResponse Добавленная ссылка.
     */
    @Operation(summary = "Добавить отслеживание ссылки")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Ссылка успешно добавлена",
                        content = @Content(schema = @Schema(implementation = LinkResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @PostMapping("/links")
    public LinkResponse addLink(@RequestHeader(TG_CHAT_ID_HEADER) Long chatId, @RequestBody AddLinkRequest request) {
        String sanitizedChatId = sanitizeInput(chatId.toString());
        String sanitizedLink = sanitizeInput(String.valueOf(request.link()));
        log.debug("Adding link for chat: {}, url: {}", sanitizedChatId, sanitizedLink);
        LinkResponse linkResponse = new LinkResponse(null, request.link(), request.tags(), request.filters());
        linkService.addLink(chatId, linkResponse);
        return linkResponse;
    }

    /**
     * Удаляет отслеживаемую ссылку для указанного чата.
     *
     * @param chatId Идентификатор чата (передается в заголовке запроса).
     * @param request Запрос на удаление ссылки, содержащий URL.
     */
    @Operation(summary = "Убрать отслеживание ссылки")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Ссылка успешно убрана"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Ссылка не найдена",
                        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @DeleteMapping("/links")
    @ResponseStatus(HttpStatus.OK)
    public void removeLink(@RequestHeader(TG_CHAT_ID_HEADER) Long chatId, @RequestBody RemoveLinkRequest request) {
        String sanitizedChatId = sanitizeInput(chatId.toString());
        String sanitizedLink = sanitizeInput(String.valueOf(request.link()));
        log.debug("Removing link for chat: {}, url: {}", sanitizedChatId, sanitizedLink);
        linkService.removeLink(chatId, request.link());
    }

    /**
     * Получает все теги, связанные с указанным чатом.
     *
     * @param chatId Идентификатор чата (передается в заголовке запроса).
     * @return Set<String> Множество тегов, связанных с чатом.
     */
    @Operation(summary = "Получить все теги чата")
    @GetMapping("/tags")
    public Set<String> getAllTags(@RequestHeader(TG_CHAT_ID_HEADER) Long chatId) {
        String sanitizedChatId = sanitizeInput(chatId.toString());
        log.debug("Getting tags for chat: {}", sanitizedChatId);
        return linkService.getAllTags(chatId);
    }

    /**
     * Получает ссылки по указанным тегам для определенного чата.
     *
     * @param chatId Идентификатор чата (передается в заголовке запроса).
     * @param tags Множество тегов для фильтрации ссылок.
     * @return ListLinksResponse Список ссылок, соответствующих указанным тегам.
     */
    @Operation(summary = "Получить ссылки по тегам")
    @GetMapping("/links/tags")
    public ListLinksResponse getLinksByTags(
            @RequestHeader(TG_CHAT_ID_HEADER) Long chatId, @RequestParam Set<String> tags) {
        String sanitizedChatId = sanitizeInput(chatId.toString());
        String sanitizedTags = sanitizeInput(tags.toString());
        log.debug("Getting links by tags for chat: {}, tags: {}", sanitizedChatId, sanitizedTags);
        Set<LinkResponse> links = linkService.getLinksByTags(chatId, tags);
        return new ListLinksResponse(List.copyOf(links), links.size());
    }

    /**
     * Обрабатывает исключения, возникающие при выполнении запросов.
     *
     * @param ex Исключение, вызванное ошибкой в процессе обработки запроса.
     * @return ApiErrorResponse Ответ с информацией об ошибке.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleException(Exception ex) {
        log.error("Error processing request", ex);
        return new ApiErrorResponse(
                "Bad request", HttpStatus.BAD_REQUEST.toString(), ex.getClass().getSimpleName(), ex.getMessage(), null);
    }
}
