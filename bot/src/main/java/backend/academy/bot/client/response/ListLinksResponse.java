package backend.academy.bot.client.response;

import java.util.List;

/**
 * Представляет объект ответа, содержащий список ссылок и их общее количество. Эта запись (record) инкапсулирует список
 * ссылок в виде объектов {@link LinkResponse} и размер списка (количество ссылок).
 *
 * @param links Список объектов {@link LinkResponse}, представляющих информацию о каждой ссылке. Каждый объект содержит
 *     данные о конкретной ссылке, такие как URI, теги и фильтры.
 * @param size Количество ссылок в списке. Это поле указывает размер коллекции ссылок.
 */
public record ListLinksResponse(List<LinkResponse> links, int size) {}
