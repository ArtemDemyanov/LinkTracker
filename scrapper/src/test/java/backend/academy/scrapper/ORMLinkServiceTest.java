package backend.academy.scrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import backend.academy.scrapper.controller.response.LinkResponse;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"app.app.access-type=ORM", "spring.jpa.hibernate.ddl-auto=validate"})
public class ORMLinkServiceTest extends AbstractIntegrationTest {

    @Autowired
    private LinkService linkService;

    @Test
    void shouldRegisterChat() {
        Long chatId = 1L;
        linkService.registerChat(chatId);

        Set<LinkResponse> links = linkService.getLinks(chatId);
        assertTrue(links.isEmpty());
    }

    @Test
    void shouldAddAndRemoveLink() {
        Long chatId = 2L;
        linkService.registerChat(chatId);

        URI url = URI.create("https://stackoverflow.com/questions/123");
        LinkResponse link = new LinkResponse(1L, url, Set.of("java", "spring"), Set.of("filter1"));

        linkService.addLink(chatId, link);

        Set<LinkResponse> links = linkService.getLinks(chatId);
        assertEquals(1, links.size());
        assertTrue(links.stream().anyMatch(l -> l.url().equals(url)));

        linkService.removeLink(chatId, url);
        links = linkService.getLinks(chatId);
        assertTrue(links.isEmpty());
    }

    @Test
    void shouldUpdateLinkData() {
        Long chatId = 4L;
        linkService.registerChat(chatId);

        URI url = URI.create("https://github.com/user/repo");
        LinkResponse link = new LinkResponse(1L, url, Set.of("backend"), Set.of("filter2"));
        linkService.addLink(chatId, link);

        // Получаем ID добавленной ссылки
        Set<LinkResponse> links = linkService.getLinks(chatId);
        Long linkId = links.iterator().next().id();

        // Обновляем данные
        String newLastUpdated = "2023-01-01T00:00Z";
        linkService.updateLastUpdated(linkId, newLastUpdated);

        String lastUpdated = linkService.getLastUpdated(linkId);
        assertEquals(newLastUpdated, lastUpdated);

        // Обновляем дату активности
        String newLastActivityDate = "2023-01-02T00:00Z";
        linkService.updateLastActivityDate(linkId, newLastActivityDate);

        String lastActivityDate = linkService.getLastActivityDate(linkId);
        assertEquals(newLastActivityDate, lastActivityDate);
    }
}
