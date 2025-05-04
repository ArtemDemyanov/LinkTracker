package backend.academy.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.dto.response.LinkResponse;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

@SpringBootTest(
        properties = {
            "resilience4j.circuitbreaker.instances.scrapperClient.sliding-window-size=5",
            "resilience4j.circuitbreaker.instances.scrapperClient.minimum-number-of-calls=3",
            "resilience4j.circuitbreaker.instances.scrapperClient.failure-rate-threshold=50",
            "resilience4j.circuitbreaker.instances.scrapperClient.permitted-number-of-calls-in-half-open-state=2",
            "resilience4j.circuitbreaker.instances.scrapperClient.wait-duration-in-open-state=2s",
            "resilience4j.timelimiter.instances.scrapperClient.timeout-duration=10s"
        })
@ContextConfiguration(initializers = RetryTest.WireMockInit.class)
class RetryTest {

    // поднимаем WireMock на порте 8081
    @RegisterExtension
    static WireMockExtension wm =
            WireMockExtension.newInstance().options(wireMockConfig().port(8081)).build();

    @Autowired
    private ScrapperClient client;

    static class WireMockInit implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            TestPropertyValues.of("app.scrapper-url=http://localhost:8081").applyTo(ctx.getEnvironment());
        }
    }

    @Test
    void retryOnServerErrors_thenEventualSuccess() {
        // задаём сценарий: 2 ошибки 500 подряд, затем 200
        wm.stubFor(post(urlPathMatching("/links"))
                .inScenario("retry-scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("second"));

        wm.stubFor(post("/links")
                .inScenario("retry-scenario")
                .whenScenarioStateIs("second")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("third"));

        wm.stubFor(post("/links")
                .inScenario("retry-scenario")
                .whenScenarioStateIs("third")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"url\":\"http://foo\",\"tags\":[],\"filters\":[]}")));

        StepVerifier.create(client.addLink(123L, new LinkResponse(null, URI.create("http://foo"), Set.of(), Set.of())))
                .expectNextMatches(resp -> resp.id() == 1)
                .verifyComplete();

        // проверяем, что всего 3 попытки
        wm.verify(3, postRequestedFor(urlEqualTo("/links")));
    }

    @Test
    void noRetryOnClientError() {
        wm.stubFor(post("/links").willReturn(aResponse().withStatus(400)));

        StepVerifier.create(client.addLink(123L, new LinkResponse(null, URI.create("http://bar"), Set.of(), Set.of())))
                .expectErrorMatches(throwable -> throwable instanceof WebClientResponseException.BadRequest)
                .verify();

        // только одна попытка
        wm.verify(1, postRequestedFor(urlEqualTo("/links")));
    }
}
