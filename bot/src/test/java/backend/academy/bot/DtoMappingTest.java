package backend.academy.bot;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.dto.request.AddLinkRequest;
import backend.academy.dto.request.LinkUpdateRequest;
import backend.academy.dto.request.RemoveLinkRequest;
import backend.academy.dto.response.LinkResponse;
import backend.academy.dto.response.ListLinksResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.junit.jupiter.api.Test;

class DtoMappingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void addLinkRequestMapping() throws Exception {
        String json = "{\"link\":\"http://example.com\",\"tags\":[\"tag1\",\"tag2\"],\"filters\":[\"f1\",\"f2\"]}";
        AddLinkRequest dto = objectMapper.readValue(json, AddLinkRequest.class);
        assertThat(dto.link()).isEqualTo(new URI("http://example.com"));
        assertThat(dto.tags()).containsExactly("tag1", "tag2");
        assertThat(dto.filters()).containsExactly("f1", "f2");
    }

    @Test
    void removeLinkRequestMapping() throws Exception {
        String json = "{\"link\":\"http://example.com\"}";
        RemoveLinkRequest dto = objectMapper.readValue(json, RemoveLinkRequest.class);
        assertThat(dto.link()).isEqualTo(new URI("http://example.com"));
    }

    @Test
    void linkResponseMapping() throws Exception {
        String json = "{\"id\":1,\"url\":\"http://example.com\",\"tags\":[\"t1\"],\"filters\":[\"f1\"]}";
        LinkResponse dto = objectMapper.readValue(json, LinkResponse.class);
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.url()).isEqualTo(new URI("http://example.com"));
        assertThat(dto.tags()).containsExactly("t1");
        assertThat(dto.filters()).containsExactly("f1");
    }

    @Test
    void listLinksResponseMapping() throws Exception {
        String json = "{\"links\":[{\"id\":1,\"url\":\"http://u\",\"tags\":[],\"filters\":[]}],\"size\":1}";
        ListLinksResponse dto = objectMapper.readValue(json, ListLinksResponse.class);
        assertThat(dto.links()).hasSize(1);
        assertThat(dto.size()).isEqualTo(1);
    }

    @Test
    void linkUpdateMapping() throws Exception {
        String json = "{\"id\":2,\"url\":\"http://u\",\"description\":\"desc\",\"tgChatIds\":[100,200]}";
        LinkUpdateRequest dto = objectMapper.readValue(json, LinkUpdateRequest.class);
        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.url()).isEqualTo(new URI("http://u"));
        assertThat(dto.description()).isEqualTo("desc");
        assertThat(dto.tgChatIds()).containsExactly(100L, 200L);
    }
}
