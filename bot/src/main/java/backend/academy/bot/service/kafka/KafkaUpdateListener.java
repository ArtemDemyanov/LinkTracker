package backend.academy.bot.service.kafka;

import backend.academy.dto.request.LinkUpdateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class KafkaUpdateListener {

    private final TelegramBot telegramBot;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.updates}", groupId = "bot-consumer")
    public void listen(ConsumerRecord<String, String> record) throws JsonProcessingException {
        LinkUpdateRequest update = objectMapper.readValue(record.value(), LinkUpdateRequest.class);
        for (Long chatId : update.tgChatIds()) {
            telegramBot.execute(new SendMessage(chatId, update.description()));
        }
        log.info("Kafka: Notification sent to {} chats", update.tgChatIds().size());
    }
}
