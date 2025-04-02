package backend.academy.scrapper.config;

import backend.academy.scrapper.domain.jdbc.dao.ChatDao;
import backend.academy.scrapper.domain.jdbc.dao.ChatLinkDao;
import backend.academy.scrapper.domain.jdbc.dao.LinkDao;
import backend.academy.scrapper.domain.jdbc.dao.LinkTagDao;
import backend.academy.scrapper.domain.jdbc.dao.TagDao;
import backend.academy.scrapper.domain.jpa.repository.ChatRepository;
import backend.academy.scrapper.domain.jpa.repository.LinkRepository;
import backend.academy.scrapper.domain.jpa.repository.TagRepository;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.ORMLinkService;
import backend.academy.scrapper.service.SQLLinkService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.app", name = "access-type", havingValue = "ORM")
    public LinkService ormLinkService(
            LinkRepository linkRepository, TagRepository tagRepository, ChatRepository chatRepository) {
        return new ORMLinkService(linkRepository, tagRepository, chatRepository);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.app", name = "access-type", havingValue = "SQL")
    public LinkService sqlLinkService(
            ChatDao chatDao, LinkDao linkDao, TagDao tagDao, LinkTagDao linkTagDao, ChatLinkDao chatLinkDao) {
        return new SQLLinkService(chatDao, linkDao, tagDao, linkTagDao, chatLinkDao);
    }
}
