package backend.academy.scrapper.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public abstract class BaseApiClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected final WebClient webClient;

    protected BaseApiClient(
            WebClient.Builder webClientBuilder, String baseUrl, Duration connectionTimeout, Duration readTimeout) {

        HttpClient httpClient = createHttpClient(connectionTimeout, readTimeout);

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    protected HttpClient createHttpClient(Duration connectionTimeout, Duration readTimeout) {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectionTimeout.toMillis())
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler((int) readTimeout.toSeconds())))
                .responseTimeout(readTimeout);
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
