package backend.academy.bot;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.commands.impl.ListCommandHandler;
import backend.academy.bot.model.TrackedLink;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ListCommandHandlerTest {

    @Mock
    private ScrapperClient scrapperClient;

    @InjectMocks
    private ListCommandHandler listCommandHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_ShouldReturnFormattedListOfLinks() {
        // Arrange
        Long chatId = 123L;
        TrackedLink link1 =
                new TrackedLink(URI.create("https://github.com/user/repo1"), Set.of("tag1"), Set.of("filter1"));
        TrackedLink link2 = new TrackedLink(URI.create("https://github.com/user/repo2"), Set.of(), Set.of());
        when(scrapperClient.getLinks(chatId)).thenReturn(List.of(link1, link2));

        // Act
        String result = listCommandHandler.handle(chatId, "/list");

        // Assert
        String expected = "Отслеживаемые ссылки:\n" + "https://github.com/user/repo1 | Tags: tag1 | Filters: filter1\n"
                + "https://github.com/user/repo2";
        assertEquals(expected, result);
    }
}
