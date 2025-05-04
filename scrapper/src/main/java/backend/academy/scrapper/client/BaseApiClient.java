package backend.academy.scrapper.client;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public abstract class BaseApiClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected final WebClient webClient;

    protected BaseApiClient(WebClient.Builder webClientBuilder, String baseUrl, Map<String, String> defaultHeaders) {

        WebClient.Builder builder =
                webClientBuilder.baseUrl(baseUrl).filter(logRequest()).filter(logResponse());

        if (defaultHeaders != null) {
            defaultHeaders.forEach(builder::defaultHeader);
        }

        this.webClient = builder.build();
    }

    protected ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    protected ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.debug("Response status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
