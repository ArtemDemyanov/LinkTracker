package backend.academy.bot.model;

import java.net.URI;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Модель отслеживаемой ссылки. Этот класс представляет ссылку, которая отслеживается ботом, вместе с её тегами и
 * фильтрами.
 */
@Getter
@Setter
public class TrackedLink {

    /** URL ссылки. */
    private URI url;

    /** Набор тегов, связанных со ссылкой. */
    private Set<String> tags;

    /** Набор фильтров, связанных со ссылкой. */
    private Set<String> filters;

    /**
     * Конструктор класса TrackedLink.
     *
     * @param url URL ссылки.
     * @param tags Набор тегов, связанных со ссылкой.
     * @param filters Набор фильтров, связанных со ссылкой.
     */
    public TrackedLink(URI url, Set<String> tags, Set<String> filters) {
        this.url = url;
        this.tags = tags;
        this.filters = filters;
    }
}
