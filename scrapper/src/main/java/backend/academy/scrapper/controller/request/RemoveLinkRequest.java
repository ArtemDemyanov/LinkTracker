package backend.academy.scrapper.controller.request;

import java.net.URI;

/**
 * Запрос на удаление ссылки.
 *
 * @param link URL ссылки, которую необходимо удалить.
 */
public record RemoveLinkRequest(URI link) {}
