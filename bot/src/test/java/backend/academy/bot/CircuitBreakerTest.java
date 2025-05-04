package backend.academy.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.*;

import backend.academy.bot.client.ScrapperClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest(
        properties = {
            "app.scrapper-url=http://localhost:8081",
            // на таймаут 2 секунды
            "resilience4j.timelimiter.instances.scrapperClient.timeout-duration=2s",
            // сразу открываем CB после первой ошибки/таймаута
            "resilience4j.circuitbreaker.instances.scrapperClient.sliding-window-type=COUNT_BASED",
            "resilience4j.circuitbreaker.instances.scrapperClient.sliding-window-size=1",
            "resilience4j.circuitbreaker.instances.scrapperClient.failure-rate-threshold=1"
        })
class CircuitBreakerTest {

    @RegisterExtension
    static WireMockExtension wm =
            WireMockExtension.newInstance().options(wireMockConfig().port(8081)).build();

    @Autowired
    private ScrapperClient client;

    @Test
    void circuitBreakerTriggersOnTimeoutAndReturnsFallback() {
        // Ставим только задержку 5 сек, но статус 200
        wm.stubFor(post("/Tg-Chat-Id/123")
                .willReturn(aResponse().withFixedDelay(5000).withStatus(200)));

        long start = System.currentTimeMillis();

        StepVerifier.create(client.registerChat(123L))
                // fallback должен вернуть пустой Mono<Void>
                .expectNextCount(0)
                .verifyComplete();

        long duration = System.currentTimeMillis() - start;
        // должен завершиться до истечения 2 сек + небольшой запас
        assertThat(duration).isLessThan(2500);
    }

    @Test
    void circuitBreakerOpensAfterFailureAndShortCircuitsSubsequentCalls() {
        wm.stubFor(post("/Tg-Chat-Id/123")
                .inScenario("cb-scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                // 1-й вызов: сразу 500 → CircuitBreaker открывается
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("OPEN"));

        wm.stubFor(post("/Tg-Chat-Id/123")
                .inScenario("cb-scenario")
                .whenScenarioStateIs("OPEN")
                // 2-й вызов: ставим большую задержку, чтобы убедиться,
                // что мы НЕ ждём ответа из WireMock
                .willReturn(aResponse().withFixedDelay(5000).withStatus(200)));

        // 1-й вызов: получаем fallback (Mono.empty) после HTTP 500
        StepVerifier.create(client.registerChat(123L)).expectNextCount(0).verifyComplete();

        long start = System.currentTimeMillis();

        // 2-й вызов: CircuitBreaker открыт → короткое замыкание
        StepVerifier.create(client.registerChat(123L)).expectNextCount(0).verifyComplete();

        long duration = System.currentTimeMillis() - start;
        // Проверяем, что фолбэк отработал тут же, без 5 сек ожидания
        assertThat(duration).isLessThan(500); // например, <0.5 сек
    }
}
