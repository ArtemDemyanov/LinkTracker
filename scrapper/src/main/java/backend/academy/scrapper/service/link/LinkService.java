package backend.academy.scrapper.service.link;

import backend.academy.dto.response.LinkResponse;
import java.net.URI;
import java.util.List;
import java.util.Set;

public interface LinkService {

    void registerChat(Long chatId); // Add this method if needed

    void addLink(Long chatId, LinkResponse link);

    void removeLink(Long chatId, URI url);

    Set<LinkResponse> getLinks(Long chatId);

    String getLastUpdated(Long linkId);

    void updateLastUpdated(Long linkId, String lastUpdated);

    List<Long> getChatIdsByLinkId(Long linkId);

    String getLastActivityDate(Long linkId);

    void updateLastActivityDate(Long linkId, String lastActivityDate);

    Set<LinkResponse> getLinksBatch(int page, int batchSize);

    Set<String> getAllTags(Long chatId);

    Set<LinkResponse> getLinksByTags(Long chatId, Set<String> tags);

    void deleteChat(Long chatId);
}
