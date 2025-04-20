package backend.academy.bot.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.app.message-transport", havingValue = "KAFKA")
public class KafkaDlqListener {

    @KafkaListener(topics = "${kafka.topics.dlq}", groupId = "bot-dlq-consumer")
    public void listenDlq(ConsumerRecord<String, String> record) {
        log.warn("⚠️ Получено сообщение в DLQ (dead letter queue): {}", record.value());
    }
}
