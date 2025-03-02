package backend.academy.bot.client.request;

import java.net.URI;

/**
 * Представляет объект запроса для удаления ссылки. Эта запись (record) инкапсулирует URI ссылки, которая должна быть
 * удалена.
 *
 * @param link URI ссылки, подлежащей удалению. Это основной идентификатор ресурса.
 */
public record RemoveLinkRequest(URI link) {}
