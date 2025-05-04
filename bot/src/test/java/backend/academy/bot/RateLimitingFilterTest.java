package backend.academy.bot;

import backend.academy.dto.request.LinkUpdateRequest;
import com.pengrad.telegrambot.TelegramBot;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(
        properties = {"app.message-transport=HTTP", "rate-limiting.enabled=true", "rate-limiting.requests-per-minute=5"
        })
public class RateLimitingFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private TelegramBot telegramBot;

    @Test
    void shouldReturn429WhenRateLimitExceeded() {
        var request = new LinkUpdateRequest(null, URI.create("https://example.com"), "DDoS test", List.of(123456L));

        for (int i = 1; i <= 5; i++) {
            webTestClient
                    .post()
                    .uri("/links")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        webTestClient
                .post()
                .uri("/links")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isEqualTo(429);
    }
}
