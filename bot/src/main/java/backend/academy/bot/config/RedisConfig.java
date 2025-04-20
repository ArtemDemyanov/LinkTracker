package backend.academy.bot.config;

import backend.academy.dto.response.LinkResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, List<LinkResponse>> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, List<LinkResponse>> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, LinkResponse.class);

        Jackson2JsonRedisSerializer<List<LinkResponse>> valueSerializer = new Jackson2JsonRedisSerializer<>(type);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
