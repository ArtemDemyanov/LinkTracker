package backend.academy.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.service.cache.CachedLinkService;
import backend.academy.dto.response.LinkResponse;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

@SpringBootTest(properties = {"spring.cache.type=redis"})
@Import(TestcontainersConfiguration.class)
class CachedLinkServiceTest {

    @MockitoBean
    private ScrapperClient scrapperClient;

    @Autowired
    private CachedLinkService cachedLinkService;

    @Autowired
    private RedisTemplate<String, List<LinkResponse>> redisTemplate;

    @Test
    void shouldCacheLinksAndInvalidateOnEvict() {
        long chatId = 123L;
        when(scrapperClient.getLinks(chatId))
                .thenReturn(
                        Mono.just(List.of(new LinkResponse(1L, URI.create("http://u1"), Set.of("t"), Set.of("f")))));

        // first call: cold, should call scrapperClient
        List<LinkResponse> first = cachedLinkService.getLinks(chatId).block();
        assertThat(first).hasSize(1);
        verify(scrapperClient, times(1)).getLinks(chatId);

        // second call: should hit cache, no additional scrapper call
        List<LinkResponse> second = cachedLinkService.getLinks(chatId).block();
        assertThat(second).isEqualTo(first);
        verifyNoMoreInteractions(scrapperClient);

        // invalidate cache
        cachedLinkService.evictCache(chatId);

        // third call: should call scrapperClient again
        List<LinkResponse> third = cachedLinkService.getLinks(chatId).block();
        assertThat(third).hasSize(1);
        verify(scrapperClient, times(2)).getLinks(chatId);
    }
}
