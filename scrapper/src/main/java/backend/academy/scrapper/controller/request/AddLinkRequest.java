package backend.academy.scrapper.controller.request;

import java.net.URI;
import java.util.Set;

/**
 * Запрос на добавление новой ссылки.
 *
 * @param link URL ссылки.
 * @param tags Набор тегов, связанных со ссылкой.
 * @param filters Набор фильтров, связанных со ссылкой.
 */
public record AddLinkRequest(URI link, Set<String> tags, Set<String> filters) {}
