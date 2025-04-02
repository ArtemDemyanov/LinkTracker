package backend.academy.scrapper.domain.jdbc.dto;

import java.net.URI;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Link {

    private Long id;
    private String url;
    private String filter;
    private OffsetDateTime createdAt;
    private OffsetDateTime checkedAt;
    private OffsetDateTime updatedAt;

    public Link withCheckedAt(OffsetDateTime checkedAt) {
        return new Link(id, url, filter, createdAt, checkedAt, updatedAt);
    }

    public Link withUpdatedAt(OffsetDateTime updatedAt) {
        return new Link(id, url, filter, createdAt, checkedAt, updatedAt);
    }

    public URI getUri() {
        return URI.create(url);
    }
}
