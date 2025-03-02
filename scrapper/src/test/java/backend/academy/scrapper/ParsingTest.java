package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.StackOverflowClient;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class ParsingTest {

    @Test
    void testParseGitHubUrl_Success() {
        ScrapperConfig scrapperConfig =
                new ScrapperConfig("", new ScrapperConfig.StackOverflowCredentials("", ""), "", "");
        GitHubClient gitHubClient = new GitHubClient(WebClient.builder(), scrapperConfig);
        URI url = URI.create("https://github.com/owner/repo");

        String[] pathParts = url.getPath().split("/");
        assertEquals("owner", pathParts[1]);
        assertEquals("repo", pathParts[2]);
    }

    @Test
    void testParseStackOverflowUrl_Success() {
        ScrapperConfig scrapperConfig =
                new ScrapperConfig("", new ScrapperConfig.StackOverflowCredentials("", ""), "", "");
        StackOverflowClient stackOverflowClient = new StackOverflowClient(WebClient.builder(), scrapperConfig);
        URI url = URI.create("https://stackoverflow.com/questions/12345");

        String[] pathParts = url.getPath().split("/");
        assertEquals("questions", pathParts[1]);
        assertEquals("12345", pathParts[2]);
    }

    @Test
    void testHandleHttpError() {
        ScrapperConfig scrapperConfig =
                new ScrapperConfig("", new ScrapperConfig.StackOverflowCredentials("", ""), "", "");
        GitHubClient gitHubClient = new GitHubClient(WebClient.builder(), scrapperConfig);
        URI url = URI.create("https://github.com/invalid/repo");

        gitHubClient
                .getLastUpdated(url)
                .doOnError(error -> {
                    assertTrue(error instanceof RuntimeException);
                    assertTrue(error.getMessage().contains("Ошибка от GitHub API"));
                })
                .subscribe();
    }
}
