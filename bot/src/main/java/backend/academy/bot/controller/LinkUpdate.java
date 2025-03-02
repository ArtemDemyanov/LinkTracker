package backend.academy.bot.controller;

import java.net.URI;
import java.util.List;

/**
 * Представляет объект обновления ссылки. Этот класс используется для передачи информации об обновлении ссылки, включая
 * её идентификатор, URL, описание и список чатов, которые должны быть уведомлены.
 *
 * @param id Уникальный идентификатор ссылки.
 * @param url URL ссылки.
 * @param description Описание обновления ссылки.
 * @param tgChatIds Список идентификаторов чатов, которые должны быть уведомлены об обновлении.
 */
public record LinkUpdate(Long id, URI url, String description, List<Long> tgChatIds) {}
