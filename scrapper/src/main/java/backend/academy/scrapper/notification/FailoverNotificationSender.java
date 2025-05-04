package backend.academy.scrapper.notification;

import backend.academy.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.config.ScrapperConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class FailoverNotificationSender implements NotificationSender {

    private final KafkaNotificationSender kafkaSender;
    private final HttpNotificationSender httpSender;
    private final ScrapperConfig config;

    @Override
    public void sendNotification(LinkUpdateRequest request) {
        String preferred = config.app().messageTransport();

        try {
            if ("KAFKA".equalsIgnoreCase(preferred)) {
                kafkaSender.sendNotification(request);
            } else {
                httpSender.sendNotification(request);
            }
        } catch (Exception ex) {
            log.error("Primary notification transport {} failed. Switching to fallback.", preferred, ex);

            try {
                if ("KAFKA".equalsIgnoreCase(preferred)) {
                    httpSender.sendNotification(request); // fallback
                } else {
                    kafkaSender.sendNotification(request); // fallback
                }
            } catch (Exception fallbackEx) {
                log.error("Fallback transport also failed", fallbackEx);
            }
        }
    }
}
