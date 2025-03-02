package backend.academy.scrapper.controller;

import backend.academy.scrapper.controller.request.AddLinkRequest;
import backend.academy.scrapper.controller.request.RemoveLinkRequest;
import backend.academy.scrapper.controller.response.ApiErrorResponse;
import backend.academy.scrapper.controller.response.LinkResponse;
import backend.academy.scrapper.controller.response.ListLinksResponse;
import backend.academy.scrapper.repository.LinkRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для управления ссылками. Этот класс предоставляет API для регистрации чатов, добавления и удаления ссылок,
 * а также получения списка ссылок.
 */
@RestController
@RequestMapping("/")
public class ScrapperController {

    private static final Logger logger = LoggerFactory.getLogger(ScrapperController.class);
    private final LinkRepository linkRepository;

    /**
     * Конструктор класса ScrapperController.
     *
     * @param linkRepository Репозиторий для управления ссылками.
     */
    public ScrapperController(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    /**
     * Регистрирует чат.
     *
     * @param id Уникальный идентификатор чата.
     * @return ResponseEntity с кодом состояния HTTP 200.
     */
    @Operation(summary = "Зарегистрировать чат")
    @ApiResponse(responseCode = "200", description = "Чат зарегистрирован")
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping(value = "/tg-chat/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> registerChat(@PathVariable Long id) {
        logger.atInfo().setMessage("Chat registered").addKeyValue("chatId", id).log();
        return ResponseEntity.ok().build();
    }

    /**
     * Удаляет чат.
     *
     * @param id Уникальный идентификатор чата.
     * @return ResponseEntity с кодом состояния HTTP 200.
     */
    @Operation(summary = "Удалить чат")
    @ApiResponse(responseCode = "200", description = "Чат успешно удалён")
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Чат не существует",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping(value = "/tg-chat/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        logger.atInfo().setMessage("Chat deleted").addKeyValue("chatId", id).log();
        linkRepository.getLinks(id).clear();
        return ResponseEntity.ok().build();
    }

    /**
     * Получает все отслеживаемые ссылки для указанного чата.
     *
     * @param chatId Уникальный идентификатор чата.
     * @return ResponseEntity со списком ссылок.
     */
    @Operation(summary = "Получить все отслеживаемые ссылки")
    @ApiResponse(
            responseCode = "200",
            description = "Ссылки успешно получены",
            content = @Content(schema = @Schema(implementation = ListLinksResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping(value = "/links", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        Set<LinkResponse> links = linkRepository.getLinks(chatId);
        logger.atDebug()
                .setMessage("Retrieved links for chat")
                .addKeyValue("chatId", chatId)
                .addKeyValue("linksCount", links.size())
                .log();
        return ResponseEntity.ok(new ListLinksResponse(List.copyOf(links), links.size()));
    }

    /**
     * Добавляет новую ссылку для отслеживания.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param request Запрос на добавление ссылки.
     * @return ResponseEntity с информацией о добавленной ссылке.
     */
    @Operation(summary = "Добавить отслеживание ссылки")
    @ApiResponse(
            responseCode = "200",
            description = "Ссылка успешно добавлена",
            content = @Content(schema = @Schema(implementation = LinkResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping(value = "/links", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {
        LinkResponse link =
                new LinkResponse(linkRepository.generateId(), request.link(), request.tags(), request.filters());
        linkRepository.addLink(chatId, link);
        logger.atInfo()
                .setMessage("Link added")
                .addKeyValue("chatId", chatId)
                .addKeyValue("linkId", link.id())
                .addKeyValue("url", link.url())
                .log();
        return ResponseEntity.ok(link);
    }

    /**
     * Удаляет ссылку из отслеживания.
     *
     * @param chatId Уникальный идентификатор чата.
     * @param request Запрос на удаление ссылки.
     * @return ResponseEntity с кодом состояния HTTP 200.
     */
    @Operation(summary = "Убрать отслеживание ссылки")
    @ApiResponse(
            responseCode = "200",
            description = "Ссылка успешно убрана",
            content = @Content(schema = @Schema(implementation = LinkResponse.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Ссылка не найдена",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping(value = "/links", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {
        URI url = request.link();
        linkRepository.removeLink(chatId, url);
        logger.atInfo()
                .setMessage("Link removed")
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", url)
                .log();
        return ResponseEntity.ok().build();
    }
}
