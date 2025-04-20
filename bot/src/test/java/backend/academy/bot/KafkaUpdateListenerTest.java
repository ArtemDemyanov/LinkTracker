package backend.academy.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import backend.academy.dto.request.LinkUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.backoff.FixedBackOff;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        properties = {
            "app.message-transport=KAFKA",
            "kafka.topics.updates=link-updates",
            "kafka.topics.dlq=link-updates-dlq",
            "spring.kafka.consumer.auto-offset-reset=earliest"
        })
@Testcontainers
class KafkaUpdateListenerTest {

    @Container
    static org.testcontainers.containers.KafkaContainer kafka =
            new org.testcontainers.containers.KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private TelegramBot telegramBot;

    @Autowired
    private ObjectMapper objectMapper;

    private KafkaConsumer<String, String> dlqConsumer;

    @BeforeEach
    void setupDlqConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafka.getBootstrapServers());
        props.put("group.id", "dlq-test-group-" + UUID.randomUUID());
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("auto.offset.reset", "earliest");
        dlqConsumer = new KafkaConsumer<>(props);
        dlqConsumer.subscribe(Collections.singletonList("link-updates-dlq"));
    }

    @AfterEach
    void closeConsumer() {
        dlqConsumer.close();
    }

    @Test
    void validMessageIsProcessed() throws Exception {
        LinkUpdateRequest update = new LinkUpdateRequest(10L, new URI("http://example.com"), "TestDesc", List.of(5L));
        String message = objectMapper.writeValueAsString(update);
        kafkaTemplate.send("link-updates", message);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(telegramBot).execute(any()));
    }

    @Test
    void invalidMessageGoesToDlq() {
        String invalidJson = "{ invalid }";
        kafkaTemplate.send("link-updates", invalidJson);
        ConsumerRecords<String, String> records = dlqConsumer.poll(Duration.ofSeconds(10));
        boolean found = false;
        for (ConsumerRecord<String, String> rec : records.records("link-updates-dlq")) {
            if (invalidJson.equals(rec.value())) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @TestConfiguration
    static class KafkaTestConfig {
        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
                ConsumerFactory<String, String> consumerFactory, KafkaTemplate<String, String> template) {
            ConcurrentKafkaListenerContainerFactory<String, String> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                    template,
                    (record, ex) ->
                            new org.apache.kafka.common.TopicPartition(record.topic() + "-dlq", record.partition()));
            DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(0L, 0L));
            factory.setCommonErrorHandler(errorHandler);
            return factory;
        }
    }
}
