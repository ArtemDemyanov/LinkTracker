package backend.academy.scrapper;

import static org.mockito.Mockito.*;

import backend.academy.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.AppProperties;
import backend.academy.scrapper.notification.FailoverNotificationSender;
import backend.academy.scrapper.notification.HttpNotificationSender;
import backend.academy.scrapper.notification.KafkaNotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FailoverNotificationSenderTest {

    private KafkaNotificationSender kafkaSender;
    private HttpNotificationSender httpSender;
    private ScrapperConfig config;
    private FailoverNotificationSender failoverSender;

    @BeforeEach
    void setup() {
        kafkaSender = mock(KafkaNotificationSender.class);
        httpSender = mock(HttpNotificationSender.class);

        AppProperties appProperties = mock(AppProperties.class);
        when(appProperties.messageTransport()).thenReturn("KAFKA");

        config = mock(ScrapperConfig.class);
        when(config.app()).thenReturn(appProperties);

        failoverSender = new FailoverNotificationSender(kafkaSender, httpSender, config);
    }

    @Test
    void shouldFallbackToHttpWhenKafkaFails() {
        LinkUpdateRequest request = new LinkUpdateRequest(123L, null, null, null);

        doThrow(new RuntimeException("Kafka failure")).when(kafkaSender).sendNotification(request);

        failoverSender.sendNotification(request);

        verify(kafkaSender).sendNotification(request);
        verify(httpSender).sendNotification(request);
    }

    @Test
    void shouldNotFallbackIfPrimarySucceeds() {
        LinkUpdateRequest request = new LinkUpdateRequest(123L, null, null, null);

        failoverSender.sendNotification(request);

        verify(kafkaSender).sendNotification(request);
        verify(httpSender, never()).sendNotification(any());
    }

    @Test
    void shouldLogFailureIfBothTransportsFail() {
        LinkUpdateRequest request = new LinkUpdateRequest(123L, null, null, null);

        doThrow(new RuntimeException("Kafka failure")).when(kafkaSender).sendNotification(request);
        doThrow(new RuntimeException("Http fallback failure")).when(httpSender).sendNotification(request);

        failoverSender.sendNotification(request);

        verify(kafkaSender).sendNotification(request);
        verify(httpSender).sendNotification(request);
    }
}
