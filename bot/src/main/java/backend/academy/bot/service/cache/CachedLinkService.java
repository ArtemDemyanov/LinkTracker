package backend.academy.bot.service.cache;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.dto.response.LinkResponse;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CachedLinkService {
    private final ScrapperClient scrapperClient;
    private final RedisTemplate<String, List<LinkResponse>> redisTemplate;

    public CachedLinkService(ScrapperClient scrapperClient, RedisTemplate<String, List<LinkResponse>> redisTemplate) {
        this.scrapperClient = scrapperClient;
        this.redisTemplate = redisTemplate;
    }

    public Mono<List<LinkResponse>> getLinks(Long chatId) {
        String key = "listLinks::" + chatId;

        List<LinkResponse> cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Mono.just(cached);
        }

        return scrapperClient
                .getLinks(chatId)
                .doOnNext(links -> redisTemplate.opsForValue().set(key, links));
    }

    public void evictCache(Long chatId) {
        redisTemplate.delete("listLinks::" + chatId);
    }
}
