package backend.academy.scrapper.controller.response;

import java.util.List;

/**
 * Ответ со списком ссылок.
 *
 * @param links Список ссылок.
 * @param size Количество ссылок в списке.
 */
public record ListLinksResponse(List<LinkResponse> links, int size) {}
