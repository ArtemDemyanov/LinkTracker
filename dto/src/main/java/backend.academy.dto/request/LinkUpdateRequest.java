package backend.academy.dto.request;

import java.net.URI;
import java.util.List;

/**
 * Запрос на обновление ссылки, содержащий информацию для уведомления пользователей.
 *
 * @param id Уникальный идентификатор ссылки
 * @param url URL ссылки
 * @param description Описание обновления
 * @param tgChatIds Список ID чатов Telegram для отправки уведомлений
 */
public record LinkUpdateRequest(Long id, URI url, String description, List<Long> tgChatIds) {}
