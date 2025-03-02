package backend.academy.scrapper.service.request;

import java.net.URI;
import java.util.List;

/**
 * Запрос на обновление ссылки.
 *
 * @param id Уникальный идентификатор ссылки.
 * @param url URL ссылки.
 * @param description Описание обновления.
 * @param tgChatIds Список идентификаторов чатов, которые должны быть уведомлены.
 */
public record LinkUpdateRequest(Long id, URI url, String description, List<Long> tgChatIds) {}
