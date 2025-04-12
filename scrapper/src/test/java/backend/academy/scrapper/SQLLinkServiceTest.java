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
@TestPropertySource(
        properties = {"spring.profiles.active=test", "app.app.access-type=SQL", "spring.jpa.hibernate.ddl-auto=validate"
        })
public class SQLLinkServiceTest extends AbstractIntegrationTest {

    @Autowired
    private LinkService linkService;

    @Test
    void shouldManageLinksWithTags() {
        Long chatId = 3L;
        linkService.registerChat(chatId);

        URI url = URI.create("https://github.com/user/repo");
        LinkResponse link = new LinkResponse(1L, url, Set.of("backend", "api"), Set.of("filter2"));

        linkService.addLink(chatId, link);

        Set<LinkResponse> links = linkService.getLinks(chatId);
        assertEquals(1, links.size());
        assertEquals(2, links.iterator().next().tags().size());

        linkService.removeLink(chatId, url);
        assertTrue(linkService.getLinks(chatId).isEmpty());
    }

    @Test
    void shouldUpdateLinkData() {
        Long chatId = 5L;
        linkService.registerChat(chatId);

        URI url = URI.create("https://stackoverflow.com/questions/456");
        LinkResponse link = new LinkResponse(2L, url, Set.of("database"), Set.of("filter3"));
        linkService.addLink(chatId, link);

        // Получаем ID добавленной ссылки
        Set<LinkResponse> links = linkService.getLinks(chatId);
        Long linkId = links.iterator().next().id();

        // Обновляем данные
        String newLastUpdated = "2023-02-01T00:00Z";
        linkService.updateLastUpdated(linkId, newLastUpdated);

        String lastUpdated = linkService.getLastUpdated(linkId);
        assertEquals(newLastUpdated, lastUpdated);

        // Обновляем дату активности
        String newLastActivityDate = "2023-02-02T00:00Z";
        linkService.updateLastActivityDate(linkId, newLastActivityDate);

        String lastActivityDate = linkService.getLastActivityDate(linkId);
        assertEquals(newLastActivityDate, lastActivityDate);
    }
}
