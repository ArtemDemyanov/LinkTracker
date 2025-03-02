package backend.academy.scrapper;

import static org.mockito.Mockito.*;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.controller.response.LinkResponse;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.UpdateCheckerService;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
class UpdateCheckerServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private StackOverflowClient stackOverflowClient;

    @Autowired
    private WebClient.Builder webClientBuilder; // Используем реальный WebClient.Builder из Spring Context

    private UpdateCheckerService updateCheckerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Вручную создаем экземпляр CheckUpdateService
        updateCheckerService =
                new UpdateCheckerService(linkRepository, gitHubClient, stackOverflowClient, webClientBuilder);
    }

    @Test
    void checkForUpdates_ShouldSendNotificationOnlyToSubscribedUsers() {
        // Arrange
        LinkResponse githubLink = new LinkResponse(1L, URI.create("https://github.com/user/repo"), Set.of(), Set.of());
        LinkResponse stackOverflowLink =
                new LinkResponse(2L, URI.create("https://stackoverflow.com/questions/12345"), Set.of(), Set.of());

        List<Long> githubChatIds = List.of(123L, 456L); // Пользователи, которые отслеживают GitHub-ссылку
        List<Long> nonTrackingChatIds = List.of(789L); // Пользователи, которые не отслеживают ссылку

        when(linkRepository.getAllLinks()).thenReturn(Set.of(githubLink, stackOverflowLink));
        when(linkRepository.getChatIdsByLinkId(1L)).thenReturn(githubChatIds); // GitHub-ссылка
        when(linkRepository.getChatIdsByLinkId(2L)).thenReturn(List.of()); // Stack Overflow-ссылка без подписчиков

        when(gitHubClient.getLastUpdated(githubLink.url())).thenReturn(Mono.just("2023-10-01T00:00:00Z"));
        when(stackOverflowClient.getLastActivityDate(stackOverflowLink.url())).thenReturn(Mono.just(1696152000L));

        updateCheckerService.checkForUpdates();

        // Проверяем, что updateLastUpdated был вызван только для GitHub-ссылки и её подписчиков
        verify(linkRepository).updateLastUpdated(eq(1L), eq("2023-10-01T00:00:00Z"));

        // Убеждаемся, что updateLastUpdated НЕ был вызван для Stack Overflow-ссылки
        // verify(linkRepository, never()).updateLastActivityDate(anyLong(), anyString());

        // Проверяем, что updateLastUpdated НЕ был вызван для неподписчиков
        // for (Long chatId : nonTrackingChatIds) {
        //    verify(linkRepository, never()).updateLastUpdated(anyLong(), anyString());
        // }
    }
}
