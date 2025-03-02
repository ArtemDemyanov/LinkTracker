package backend.academy.scrapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

class HttpClientTest {

    @Test
    void getLastUpdated_ShouldHandleHttpError() {
        // Arrange
        WebClient webClient = mock(WebClient.class);
        ScrapperConfig config = mock(ScrapperConfig.class);
        when(config.githubToken()).thenReturn("dummy-token");

        GitHubClient gitHubClient = new GitHubClient(WebClient.builder(), config);

        URI url = URI.create("https://github.com/user/repo");
        when(webClient.get()).thenThrow(new WebClientResponseException(404, "Not Found", null, null, null));

        StepVerifier.create(gitHubClient.getLastUpdated(url))
                .expectError(RuntimeException.class) // Ожидаем RuntimeException
                .verify();
    }

    @Test
    void getLastActivityDate_ShouldHandleHttpError() {
        // Arrange
        WebClient webClient = mock(WebClient.class);
        ScrapperConfig config = mock(ScrapperConfig.class);
        when(config.stackOverflow())
                .thenReturn(new ScrapperConfig.StackOverflowCredentials("dummy-key", "dummy-token"));

        StackOverflowClient stackOverflowClient = new StackOverflowClient(WebClient.builder(), config);

        URI url = URI.create("https://stackoverflow.com/questions/123");
        when(webClient.get()).thenThrow(new WebClientResponseException(404, "Not Found", null, null, null));

        // Act & Assert
        StepVerifier.create(stackOverflowClient.getLastActivityDate(url))
                .expectError(RuntimeException.class)
                .verify();
    }
}
