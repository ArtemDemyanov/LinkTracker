package backend.academy.scrapper.notification;

import backend.academy.dto.request.LinkUpdateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaNotificationSender implements NotificationSender {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String topicName = "link-updates";
    private final String dlqTopic = "link-updates-dlq";

    @Override
    public void sendNotification(LinkUpdateRequest request) {
        try {
            String message = objectMapper.writeValueAsString(request);
            kafkaTemplate.send(topicName, message);
            log.info("Notification sent via Kafka: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message, sending to DLQ", e);
            kafkaTemplate.send(dlqTopic, "Malformed message: " + e.getMessage());
        }
    }
}
