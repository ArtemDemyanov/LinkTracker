package backend.academy.scrapper.service.link;

import backend.academy.dto.response.LinkResponse;
import backend.academy.scrapper.domain.jdbc.dao.ChatDao;
import backend.academy.scrapper.domain.jdbc.dao.ChatLinkDao;
import backend.academy.scrapper.domain.jdbc.dao.LinkDao;
import backend.academy.scrapper.domain.jdbc.dao.LinkTagDao;
import backend.academy.scrapper.domain.jdbc.dao.TagDao;
import backend.academy.scrapper.domain.jdbc.dto.Chat;
import backend.academy.scrapper.domain.jdbc.dto.Link;
import backend.academy.scrapper.domain.jdbc.dto.LinkTag;
import backend.academy.scrapper.domain.jdbc.dto.Tag;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class SQLLinkService implements LinkService {

    private final ChatDao chatDao;
    private final LinkDao linkDao;
    private final TagDao tagDao;
    private final LinkTagDao linkTagDao;
    private final ChatLinkDao chatLinkDao;

    @Override
    @Transactional
    public void registerChat(Long chatId) {
        chatDao.save(new Chat(chatId));
    }

    @Override
    @Transactional
    public void addLink(Long chatId, LinkResponse link) {
        String filters = String.join("|", link.filters());
        Link linkDto = new Link(
                null,
                link.url().toString(),
                filters,
                OffsetDateTime.now(ZoneId.systemDefault()),
                OffsetDateTime.now(ZoneId.systemDefault()),
                OffsetDateTime.now(ZoneId.systemDefault()));
        linkDao.save(linkDto);
        Link savedLink = linkDao.findByUrl(link.url().toString())
                .orElseThrow(() -> new IllegalStateException("Link should have been saved but not found"));

        chatLinkDao.save(chatId, savedLink.id());

        for (String tagName : link.tags()) {
            Tag tagDto = new Tag(null, tagName);
            tagDao.save(tagDto);
            Tag savedTag = tagDao.findByName(tagName);

            LinkTag linkTagDto = new LinkTag(null, savedLink.id(), savedTag.id(), chatId);
            linkTagDao.save(linkTagDto);
        }
    }

    @Override
    @Transactional
    public void removeLink(Long chatId, URI url) {
        Link link = linkDao.findByUrl(url.toString())
                .orElseThrow(() -> new IllegalArgumentException("Link not found for URL: " + url));

        chatLinkDao.delete(chatId, link.id());

        List<LinkTag> linkTags = linkTagDao.findByLinkId(link.id());
        for (LinkTag linkTag : linkTags) {
            if (linkTag.chatId().equals(chatId)) {
                linkTagDao.delete(linkTag);
            }
        }

        List<Long> remainingChatIds = chatLinkDao.findChatIdsByLinkId(link.id());
        if (remainingChatIds.isEmpty()) {
            linkDao.delete(link);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LinkResponse> getLinks(Long chatId) {
        List<Link> links = linkDao.findByChatId(chatId);

        return links.stream()
                .map(link -> {
                    Set<String> tags = linkTagDao.findTagsByLinkId(link.id());
                    return new LinkResponse(link.id(), link.getUri(), tags, parseFilters(link.filter()));
                })
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public String getLastUpdated(Long linkId) {
        Link link = linkDao.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found with id: " + linkId));

        return link.updatedAt().toString();
    }

    @Override
    @Transactional
    public void updateLastUpdated(Long linkId, String lastUpdated) {
        Link link = linkDao.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found with id: " + linkId));

        linkDao.update(link.withUpdatedAt(OffsetDateTime.parse(lastUpdated)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getChatIdsByLinkId(Long linkId) {
        return chatLinkDao.findChatIdsByLinkId(linkId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLastActivityDate(Long linkId) {
        Link link = linkDao.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found with id: " + linkId));

        return link.checkedAt().toString();
    }

    @Override
    @Transactional
    public void updateLastActivityDate(Long linkId, String lastActivityDate) {
        Link link = linkDao.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found with id: " + linkId));

        linkDao.update(link.withCheckedAt(OffsetDateTime.parse(lastActivityDate)));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LinkResponse> getLinksBatch(int page, int batchSize) {
        int offset = page * batchSize;
        List<Link> links = linkDao.findBatch(offset, batchSize);

        return links.stream()
                .map(link -> {
                    Set<String> tags = linkTagDao.findTagsByLinkId(link.id());
                    return new LinkResponse(link.id(), link.getUri(), tags, parseFilters(link.filter()));
                })
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public Set<String> getAllTags(Long chatId) {
        return tagDao.findAllTagsByChatId(chatId);
    }

    @Override
    @Transactional
    public Set<LinkResponse> getLinksByTags(Long chatId, Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptySet();
        }

        return linkDao.findAllByChatIdAndTags(chatId, tags).stream()
                .map(link -> {
                    Set<String> linkTags = tagDao.findTagsByLinkAndChat(link.id(), chatId);
                    return new LinkResponse(link.id(), link.getUri(), linkTags, parseFilters(link.filter()));
                })
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId) {
        chatDao.delete(chatId);
    }

    private Set<String> parseFilters(String filterStr) {
        if (filterStr == null || filterStr.isBlank()) return Collections.emptySet();
        return Arrays.stream(filterStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
