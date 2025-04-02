package backend.academy.scrapper.service;

import backend.academy.scrapper.controller.response.LinkResponse;
import backend.academy.scrapper.domain.jpa.entity.Chat;
import backend.academy.scrapper.domain.jpa.entity.Link;
import backend.academy.scrapper.domain.jpa.entity.LinkTag;
import backend.academy.scrapper.domain.jpa.entity.Tag;
import backend.academy.scrapper.domain.jpa.repository.ChatRepository;
import backend.academy.scrapper.domain.jpa.repository.LinkRepository;
import backend.academy.scrapper.domain.jpa.repository.TagRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ORMLinkService implements LinkService {

    private final LinkRepository linkRepository;
    private final TagRepository tagRepository;
    private final ChatRepository chatRepository;
    private static final Logger logger = LoggerFactory.getLogger(ORMLinkService.class);

    @Override
    @Transactional
    public void registerChat(Long chatId) {
        try {
            if (!chatRepository.existsById(chatId)) {
                Chat chat = new Chat();
                chat.id(chatId);
                chatRepository.save(chat);
                logger.info("Chat registered successfully: {}", chatId);
            } else {
                logger.info("Chat already exists: {}", chatId);
            }
        } catch (Exception e) {
            logger.error("Error registering chat: {}", chatId, e);
            throw e; // Re-throw the exception to propagate it
        }
    }

    @Override
    @Transactional
    public void addLink(Long chatId, LinkResponse link) {
        Link existingLink = linkRepository.findByUrl(link.url()).orElse(null);

        if (existingLink != null) {
            addChatToLink(chatId, existingLink);
        } else {
            Link newLink = new Link();
            newLink.url(link.url());
            newLink.filter(String.valueOf(link.filters()));
            newLink.createdAt(OffsetDateTime.now());
            newLink.checkedAt(OffsetDateTime.now());
            newLink.updatedAt(OffsetDateTime.now());
            linkRepository.save(newLink);
            addChatToLink(chatId, newLink);
        }
        addTagsToLink(chatId, link.url(), link.tags());
    }

    @Override
    @Transactional
    public void removeLink(Long chatId, URI url) {
        Link link = linkRepository.findByUrl(url).orElseThrow(() -> new RuntimeException("Link not found"));

        removeChatFromLink(chatId, link);
        link.linkTags().removeIf(linkTag -> linkTag.chatId().equals(chatId));

        if (link.chats().isEmpty()) {
            linkRepository.delete(link);
        } else {
            linkRepository.save(link);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LinkResponse> getLinks(Long chatId) {
        Set<Link> links = linkRepository.findAllByChatId(chatId);
        return convertToLinkResponseSet(links, chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLastUpdated(Long linkId) {
        Link link = linkRepository.findById(linkId).orElseThrow(() -> new RuntimeException("Link not found"));
        return link.updatedAt().toString();
    }

    @Override
    @Transactional
    public void updateLastUpdated(Long linkId, String lastUpdated) {
        Link link = linkRepository.findById(linkId).orElseThrow(() -> new RuntimeException("Link not found"));
        link.updatedAt(OffsetDateTime.parse(lastUpdated));
        linkRepository.save(link);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getChatIdsByLinkId(Long linkId) {
        Link link = linkRepository.findById(linkId).orElseThrow(() -> new RuntimeException("Link not found"));
        return link.chats().stream().map(Chat::id).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String getLastActivityDate(Long linkId) {
        Link link = linkRepository.findById(linkId).orElseThrow(() -> new RuntimeException("Link not found"));
        return link.checkedAt().toString();
    }

    @Override
    @Transactional
    public void updateLastActivityDate(Long linkId, String lastActivityDate) {
        Link link = linkRepository.findById(linkId).orElseThrow(() -> new RuntimeException("Link not found"));
        link.checkedAt(OffsetDateTime.parse(lastActivityDate));
        linkRepository.save(link);
    }

    @Transactional(readOnly = true)
    public Set<String> getTagsForLinkAndChat(Long chatId, URI url) {
        Link link = linkRepository.findByUrl(url).orElseThrow(() -> new RuntimeException("Link not found"));

        return link.linkTags().stream()
                .filter(linkTag -> linkTag.chatId().equals(chatId))
                .map(linkTag -> linkTag.tag().name())
                .collect(Collectors.toSet());
    }

    private void addTagsToLink(Long chatId, URI url, Set<String> tags) {
        Link link = linkRepository.findByUrl(url).orElseThrow(() -> new RuntimeException("Link not found"));

        for (String tagName : tags) {
            Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
                Tag newTag = new Tag();
                newTag.name(tagName);
                return tagRepository.save(newTag);
            });

            boolean tagExists = link.linkTags().stream()
                    .anyMatch(linkTag ->
                            linkTag.tag().equals(tag) && linkTag.chatId().equals(chatId));

            if (!tagExists) {
                LinkTag linkTag = new LinkTag();
                linkTag.link(link);
                linkTag.tag(tag);
                linkTag.chatId(chatId);
                link.linkTags().add(linkTag);
            }
        }

        linkRepository.save(link);
    }

    private Set<LinkResponse> convertToLinkResponseSet(Set<Link> links, Long chatId) {
        return links.stream()
                .map(link -> new LinkResponse(
                        link.id(),
                        link.url(),
                        // link.createdAt(),
                        // link.checkedAt(),
                        // link.updatedAt(),
                        link.linkTags().stream()
                                .filter(linkTag ->
                                        chatId == null || linkTag.chatId().equals(chatId))
                                .map(linkTag -> linkTag.tag().name())
                                .collect(Collectors.toSet()),
                        Collections.singleton(link.filter()) // Заглушка для filters
                        ))
                .collect(Collectors.toSet());
    }

    private void addChatToLink(Long chatId, Link link) {
        boolean chatExists = link.chats().stream().anyMatch(chat -> chat.id().equals(chatId));

        if (!chatExists) {
            Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));

            chat.links().add(link);
            link.chats().add(chat);
            chatRepository.save(chat);
            linkRepository.save(link);
        }
    }

    private void removeChatFromLink(Long chatId, Link link) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));

        chat.links().remove(link);
        link.chats().remove(chat);
        chatRepository.save(chat);
        linkRepository.save(link);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LinkResponse> getLinksBatch(int page, int batchSize) {
        PageRequest pageRequest = PageRequest.of(page, batchSize);
        List<Link> links = linkRepository.findAll(pageRequest).getContent();
        return convertToLinkResponseSet(new HashSet<>(links), null);
    }

    @Override
    @Transactional
    public Set<String> getAllTags(Long chatId) {
        return linkRepository.findAllByChatId(chatId).stream()
                .flatMap(link -> link.linkTags().stream())
                .filter(lt -> lt.chatId().equals(chatId))
                .map(lt -> lt.tag().name())
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public Set<LinkResponse> getLinksByTags(Long chatId, Set<String> tags) {
        return linkRepository.findAllByChatId(chatId).stream()
                .filter(link -> link.linkTags().stream()
                        .anyMatch(lt -> lt.chatId().equals(chatId)
                                && tags.contains(lt.tag().name())))
                .map(link -> convertToLinkResponse(link, chatId))
                .collect(Collectors.toSet());
    }

    private LinkResponse convertToLinkResponse(Link link, Long chatId) {
        Set<String> tags = link.linkTags().stream()
                .filter(linkTag -> chatId == null || linkTag.chatId().equals(chatId))
                .map(linkTag -> linkTag.tag().name())
                .collect(Collectors.toSet());

        return new LinkResponse(link.id(), link.url(), tags, Collections.singleton(link.filter()));
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));

        Set<Link> links = new HashSet<>(chat.links());
        for (Link link : links) {
            link.chats().remove(chat);
            link.linkTags().removeIf(linkTag -> linkTag.chatId().equals(chatId));
            if (link.chats().isEmpty()) {
                linkRepository.delete(link);
            } else {
                linkRepository.save(link);
            }
        }
        chatRepository.delete(chat);
        logger.info("Deleted chat and associated data for chatId: {}", chatId);
    }
}
