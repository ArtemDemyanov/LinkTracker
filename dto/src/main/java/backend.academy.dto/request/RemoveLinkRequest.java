package backend.academy.dto.request;

import java.net.URI;

/**
 * DTO для представления запроса на удаление ссылки.
 *
 * @param link URI ссылки, подлежащей удалению. Это основной идентификатор ресурса.
 */
public record RemoveLinkRequest(URI link) {}
