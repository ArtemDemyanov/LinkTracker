package backend.academy.scrapper.controller.response;

import java.net.URI;
import java.util.Set;

/**
 * DTO для представления информации о ссылке.
 *
 * @param id Уникальный идентификатор ссылки в системе.
 * @param url URI ссылки. Это основной адрес ресурса.
 * @param tags Набор строк, представляющих теги, связанные со ссылкой. Теги используются для категоризации или
 *     маркировки ссылки для удобства поиска или группировки.
 * @param filters Набор строк, представляющих фильтры, связанные со ссылкой. Фильтры используются для определения
 *     условий или ограничений при обработке или отображении ссылки.
 */
public record LinkResponse(Long id, URI url, Set<String> tags, Set<String> filters) {}
