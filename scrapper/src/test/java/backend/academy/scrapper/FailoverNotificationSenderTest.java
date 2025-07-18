package backend.academy.scrapper;

import backend.academy.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.AppProperties;
import backend.academy.scrapper.notification.FailoverNotificationSender;
import backend.academy.scrapper.notification.HttpNotificationSender;
import backend.academy.scrapper.notification.KafkaNotificationSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FailoverNotificationSenderTest {

    @Mock
    private KafkaNotificationSender kafkaSender;

    @Mock
    private HttpNotificationSender httpSender;

    @Mock
    private ScrapperConfig config;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private FailoverNotificationSender failoverSender;

    @Test
    void shouldFallbackToHttpWhenKafkaFails() {
        LinkUpdateRequest request = new LinkUpdateRequest(123L, null, null, null);

        when(config.app()).thenReturn(appProperties);
        when(appProperties.messageTransport()).thenReturn("KAFKA");

        doThrow(new RuntimeException("Kafka failure")).when(kafkaSender).sendNotification(request);

        failoverSender.sendNotification(request);

        verify(kafkaSender).sendNotification(request);
        verify(httpSender).sendNotification(request);
    }

    @Test
    void shouldNotFallbackIfPrimarySucceeds() {
        LinkUpdateRequest request = new LinkUpdateRequest(123L, null, null, null);

        when(config.app()).thenReturn(appProperties);
        when(appProperties.messageTransport()).thenReturn("KAFKA");

        failoverSender.sendNotification(request);

        verify(kafkaSender).sendNotification(request);
        verify(httpSender, never()).sendNotification(any());
    }

    @Test
    void shouldLogFailureIfBothTransportsFail() {
        LinkUpdateRequest request = new LinkUpdateRequest(123L, null, null, null);

        when(config.app()).thenReturn(appProperties);
        when(appProperties.messageTransport()).thenReturn("KAFKA");

        doThrow(new RuntimeException("Kafka failure")).when(kafkaSender).sendNotification(request);
        doThrow(new RuntimeException("Http fallback failure")).when(httpSender).sendNotification(request);

        failoverSender.sendNotification(request);

        verify(kafkaSender).sendNotification(request);
        verify(httpSender).sendNotification(request);
    }
}
