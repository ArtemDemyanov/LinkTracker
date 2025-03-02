package backend.academy.scrapper.controller.response;

import java.net.URI;
import java.util.Set;

/**
 * Ответ с информацией о ссылке.
 *
 * @param id Уникальный идентификатор ссылки.
 * @param url URL ссылки.
 * @param tags Набор тегов, связанных со ссылкой.
 * @param filters Набор фильтров, связанных со ссылкой.
 */
public record LinkResponse(Long id, URI url, Set<String> tags, Set<String> filters) {}
