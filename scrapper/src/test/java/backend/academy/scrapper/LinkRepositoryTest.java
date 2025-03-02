package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.controller.response.LinkResponse;
import backend.academy.scrapper.repository.LinkRepository;
import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LinkRepositoryTest {

    @Test
    void testSaveLinkWithTagsAndFilters() {
        LinkRepository linkRepository = new LinkRepository();
        LinkResponse link = new LinkResponse(
                1L, URI.create("https://github.com/owner/repo"), Set.of("tag1", "tag2"), Set.of("filter1", "filter2"));

        linkRepository.addLink(12345L, link);
        Set<LinkResponse> links = linkRepository.getLinks(12345L);

        assertEquals(1, links.size());
        assertTrue(links.contains(link));
    }

    @Test
    void testAddAndRemoveLink() {
        LinkRepository linkRepository = new LinkRepository();
        LinkResponse link =
                new LinkResponse(1L, URI.create("https://github.com/owner/repo"), Set.of("tag1"), Set.of("filter1"));

        linkRepository.addLink(12345L, link);
        Set<LinkResponse> links = linkRepository.getLinks(12345L);
        assertEquals(1, links.size());

        linkRepository.removeLink(12345L, link.url());
        links = linkRepository.getLinks(12345L);
        assertEquals(0, links.size());
    }

    @Test
    void testAddDuplicateLink() {
        LinkRepository linkRepository = new LinkRepository();
        LinkResponse link1 =
                new LinkResponse(1L, URI.create("https://github.com/owner/repo"), Set.of("tag1"), Set.of("filter1"));
        LinkResponse link2 =
                new LinkResponse(2L, URI.create("https://github.com/owner/repo"), Set.of("tag2"), Set.of("filter2"));

        linkRepository.addLink(12345L, link1);
        linkRepository.addLink(12345L, link2);
        Set<LinkResponse> links = linkRepository.getLinks(12345L);

        assertEquals(1, links.size()); // Дубликаты не должны добавляться
    }
}
